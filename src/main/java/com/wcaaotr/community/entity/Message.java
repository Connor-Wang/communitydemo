package com.wcaaotr.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-07-02-21:27
 */
@Data
public class Message {

    private Integer id;
    private Integer fromId;
    private Integer toId;
    private String conversationId;
    private String content;
    private Integer status; // 0-未读;1-已读;2-删除
    private Date createTime;

}
