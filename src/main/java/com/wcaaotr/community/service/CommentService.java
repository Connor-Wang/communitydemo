package com.wcaaotr.community.service;

import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.dao.CommentMapper;
import com.wcaaotr.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Connor
 * @create 2021-07-02-15:20
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired(required = false)
    private CommentMapper commentMapper;
    @Autowired(required = false)
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentsByEntity(entityType, entityId);
    }

    public int findCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentCountByEntity(entityType, entityId);
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment.getTargetId() == null) {
            comment.setTargetId(0);
        }
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        int row = commentMapper.insertComment(comment);
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            int commentCount = findCommentCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), commentCount);
        }
        return row ;
    }
}
