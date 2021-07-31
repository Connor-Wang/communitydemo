package com.wcaaotr.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-07-02-15:13
 */
@Data
public class Comment {

    private Integer id;
    private Integer userId;
    private Integer entityType; // entityType 指 评论的是帖子还是评论
    private Integer entityId;
    private Integer targetId; // target 指 评论的实体的作者
    private String content;
    private Integer status;
    private Date createTime;

}
