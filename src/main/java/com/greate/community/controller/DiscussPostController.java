package com.greate.community.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.greate.community.commons.bean.QueryPageBean;
import com.greate.community.commons.result.ActionResult;
import com.greate.community.commons.result.PaginationBuilder;
import com.greate.community.commons.result.ResultBuilder;
import com.greate.community.entity.*;
import com.greate.community.event.EventProducer;
import com.greate.community.service.CommentService;
import com.greate.community.service.DiscussPostService;
import com.greate.community.service.LikeService;
import com.greate.community.service.UserService;
import com.greate.community.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 帖子
 */
@RestController
@RequestMapping("/discussPost")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private  QiniuUtils qiniuUtils;

    // 网站域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目名(访问路径)
    @Value("${server.servlet.context-path}")
    private String contextPath;


    /**
     * markdown 图片上传
     * @param imageFile
     * @return
     */
    @PostMapping("/uploadPic")
    public Map<String,Object> uploadPic(MultipartFile imageFile) {
        try{
            //获取原始文件名
            String originalFilename = imageFile.getOriginalFilename();
            //获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = CommunityUtil.generateUUID() + suffix;
            qiniuUtils.upload2Qiniu(imageFile.getBytes(),fileName);
            Map<String,Object> result = new HashMap<>();
            result.put("errno",0);
            Map<String,Object> data = new HashMap<>();
            data.put("url",qiniuUtils.getUrl()+"/"+fileName);
            result.put("data",data);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            Map<String,Object> result = new HashMap<>();
            result.put("errno",1);
            result.put("message","上传失败");
            //图片上传失败
            return result;
        }
    }

    /**
     * 添加帖子（发帖）
     * @param discussPost
     * @return
     */
    @PostMapping("/add")
    public ActionResult addDiscussPost(@RequestBody DiscussPost discussPost) {
        User user = hostHolder.getUser();
        System.out.println(user);

        if (user == null) {
            return ResultBuilder.buildActionFail(null,"还没登陆哟！！");
        }
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());

        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return ResultBuilder.buildActionSuccess(null,"发布成功");
    }

    /**
     * 根据条件搜索 帖子
     * @param queryPageBean
     * @return
     */
    @PostMapping("/findDiscussPostByCondition")
    public ActionResult findDiscussPostByCondition(@RequestBody QueryPageBean queryPageBean) {
        Map<String, Object> discussPostByCondition = discussPostService.findDiscussPostByCondition(queryPageBean.getCurrentPage(), queryPageBean.getPageSize(), queryPageBean.getQueryStrings());
        return ResultBuilder.buildActionSuccess(discussPostByCondition);
    }


    /**
     * 进入帖子详情页
     * @param discussPostId
     * @param queryPageBean
     * @return
     */
    @PostMapping("/detail/{discussPostId}")
    public ActionResult getDiscussPost(@PathVariable("discussPostId") int discussPostId, @RequestBody QueryPageBean queryPageBean) {
        Map<String,Object> map=new HashMap<>();
        // 帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        discussPost.setcTime(format.format(discussPost.getCreateTime()));
//        String content = HtmlUtils.htmlUnescape(discussPost.getContent()); // 内容反转义，不然 markDown 格式无法显示
//        discussPost.setContent(content);
        map.put("post", discussPost);
        // 作者
        User user = userService.findUserById(discussPost.getUserId());
        map.put("user", user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        map.put("likeCount", likeCount);
        // 当前登录用户的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        map.put("likeStatus", likeStatus);

        // 帖子的评论列表
        Page<Object> page = PageHelper.startPage(queryPageBean.getCurrentPage(), queryPageBean.getPageSize());
        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST, discussPost.getId());
        // 封装评论及其相关信息
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 存储对帖子的评论
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment); // 评论
                commentVo.put("user", userService.findUserById(comment.getUserId())); // 发布评论的作者
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()); // 该评论点赞数量
                commentVo.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(
                        hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()); // 当前登录用户对该评论的点赞状态
                commentVo.put("likeStatus", likeStatus);


                // 存储每个评论对应的回复（不做分页）
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId());
                List<Map<String, Object>> replyVoList = new ArrayList<>(); // 封装对评论的评论和评论的作者信息
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply); // 回复
                        replyVo.put("user", userService.findUserById(reply.getUserId())); // 发布该回复的作者
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target); // 该回复的目标用户
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount); // 该回复的点赞数量
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(
                                hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus); // 当前登录用户的点赞状态

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 每个评论对应的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        map.put("comments", commentVoList);
        LinkedHashMap<String, Integer> paginationMap = new LinkedHashMap<>();
        paginationMap.put("total", (int) page.getTotal());
        paginationMap.put("pageSize", queryPageBean.getPageSize());
        paginationMap.put("current", queryPageBean.getCurrentPage());
        map.put("pagination", paginationMap);
        return ResultBuilder.buildActionSuccess(map);

    }

    /**
     * 置顶帖子
     * @param id
     * @return
     */
    @PostMapping("/top")
    public String updateTop(int id, int type) {
        discussPostService.updateType(id, type);

        // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


    /**
     * 加精帖子
     * @param id
     * @return
     */
    @PostMapping("/wonderful")
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }


    /**
     * 删除帖子
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ActionResult setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件，通过消息队列更新 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return ResultBuilder.buildActionSuccess();
    }


}
