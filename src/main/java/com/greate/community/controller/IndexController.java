package com.greate.community.controller;

import com.greate.community.commons.bean.QueryPageBean;
import com.greate.community.commons.result.ListResult;
import com.greate.community.commons.result.ResultBuilder;
import com.greate.community.entity.DiscussPost;
import com.greate.community.entity.Page;
import com.greate.community.entity.User;
import com.greate.community.service.DiscussPostService;
import com.greate.community.service.LikeService;
import com.greate.community.service.UserService;
import com.greate.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页
 */
@RestController
public class IndexController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 进入首页
     * @param orderMode 默认是 0（最新）
     * @return
     */
    @GetMapping("/home")
    public ListResult<Object> getHomeData(@RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        // 分页查询
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, 1, 15, orderMode);
        // 封装帖子和该帖子对应的用户信息
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                post.setcTime(format.format(post.getCreateTime()));
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        return ResultBuilder.buildListSuccess(discussPosts);
    }


    /**
     * 进入 500 错误界面
     * @return
     */
    @GetMapping("/error")
    @ResponseBody
    public String getErrorPage() {
        return "error";
    }

//    /**
//     * 没有权限访问时的错误界面（也是 404）
//     * @return
//     */
//    @GetMapping("/denied")
//    public String getDeniedPage() {
//        return "/error/404";
//    }

}
