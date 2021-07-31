package com.wcaaotr.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-06-29-19:55
 */
@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private Integer type; // 0-普通用户 1-版主 2-管理员
    private String headerUrl; // 用户头像的 url
    private Date createTime;
}
