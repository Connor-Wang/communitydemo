package com.wcaaotr.community.dao;

import com.wcaaotr.community.entity.LoginToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Connor
 * @create 2021-06-30-20:58
 */
@Mapper
@Deprecated
public interface LoginTokenMapper {

    int insertLoginToken(LoginToken loginToken);

    int updateLoginTokenStatus(String token, int status);

    LoginToken selectLoginTokenByToken(String token);
}
