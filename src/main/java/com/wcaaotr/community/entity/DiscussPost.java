package com.wcaaotr.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-06-29-19:47
 */
@Data
public class DiscussPost {

    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Integer type; // 0-普通; 1-置顶
    private Integer status; // 0-正常; 1-精华; 2-拉黑
    private Date createTime;
    private Integer commentCount;
    private Double score;
}
