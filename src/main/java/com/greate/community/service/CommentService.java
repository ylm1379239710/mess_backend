package com.greate.community.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.greate.community.commons.result.PaginationBuilder;
import com.greate.community.dao.CommentMapper;
import com.greate.community.dao.UserMapper;
import com.greate.community.entity.Comment;
import com.greate.community.entity.DiscussPost;
import com.greate.community.entity.User;
import com.greate.community.util.CommunityConstant;
import com.greate.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论相关
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据 id 查询评论
     *
     * @param id
     * @return
     */
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }


    /**
     * 根据评论目标（类别、id）对评论进行分页查询
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public List<Comment> findCommentByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentByEntity(entityType, entityId);
    }

    /**
     * 查询评论的数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 分页查询某个用户的评论/回复列表
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentByUserId(int userId, int offset, int limit) {
        return commentMapper.selectCommentByUserId(userId, offset, limit);
    }

    /**
     * 查询某个用户的评论/回复数量
     *
     * @param userId
     * @return
     */
    public int findCommentCountByUserId(int userId) {
        return commentMapper.selectCommentCountByUserId(userId);
    }

    /**
     * 添加评论（需要事务管理）
     *
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // Html 标签转义
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 敏感词过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        // 添加评论
        int rows = commentMapper.insertComment(comment);

        // 更新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }


    public Map<String, Object> getAllCommentByTargetId(Integer currentPage, Integer pageSize, Map<String, Object> queryStrings) {
        Page<Object> page = PageHelper.startPage(currentPage, pageSize);
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> paginationMap = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> list = new ArrayList<>();
        List<Comment> resultList = commentMapper.getAllCommentByTargetId(queryStrings);
        for (Comment comment : resultList) {
            LinkedHashMap<String, Object> commentInfo = new LinkedHashMap<>();
            commentInfo.put("comment", comment);
            User user = userMapper.selectById(comment.getUserId());
            commentInfo.put("user", user);
            if (comment.getEntityType() == 1) {
                DiscussPost discussPostById = discussPostService.findDiscussPostById(comment.getEntityId());
                commentInfo.put("post", discussPostById);
            } else {
                Comment reply = commentMapper.selectCommentById(comment.getEntityId());
                commentInfo.put("reply", reply);
            }
            list.add(commentInfo);
        }
        resultMap.put("list", list);
        paginationMap.put("total", (int) page.getTotal());
        paginationMap.put("pageSize", pageSize);
        paginationMap.put("current", currentPage);
        resultMap.put("pagination", paginationMap);
        return resultMap;
    }

}
