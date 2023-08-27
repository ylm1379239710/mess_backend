package com.greate.community.controller;

import com.greate.community.commons.bean.QueryPageBean;
import com.greate.community.commons.result.ActionResult;
import com.greate.community.commons.result.ResultBuilder;
import com.greate.community.entity.Comment;
import com.greate.community.entity.DiscussPost;
import com.greate.community.entity.Event;
import com.greate.community.event.EventProducer;
import com.greate.community.service.CommentService;
import com.greate.community.service.DiscussPostService;
import com.greate.community.util.CommunityConstant;
import com.greate.community.util.HostHolder;
import com.greate.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 评论/回复
 */
@RestController
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加评论
     *
     * @param discussPostId
     * @param comment
     * @return
     */
    @PostMapping("/add/{discussPostId}")
    public ActionResult addComment(@PathVariable("discussPostId") int discussPostId, @RequestBody Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件（系统通知）
        Event event = new Event()
                .setTopic(TOPIC_COMMNET)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return ResultBuilder.buildActionSuccess(null, "您的评论已成功发送！");
    }

    /**
     * 获取所以评论
     *
     * @param queryPageBean
     * @return
     */
    @PostMapping("/getAllCommentByTargetId")
    public ActionResult getAllCommentByTargetId(@RequestBody QueryPageBean queryPageBean) {

        Map<String, Object> commentList = commentService.getAllCommentByTargetId(queryPageBean.getCurrentPage(),queryPageBean.getPageSize(),queryPageBean.getQueryStrings());
        return ResultBuilder.buildActionSuccess(commentList);
    }
}
