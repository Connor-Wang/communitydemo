package com.wcaaotr.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-06-30-20:57
 */
@Data
public class LoginToken {

    private Integer id;
    private Integer userId;
    private String token;
    private Integer status; //0-有效; 1-无效;
    private Date expired; // 过期时间

}
