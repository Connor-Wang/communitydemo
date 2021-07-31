package com.wcaaotr.community.dao;

import com.wcaaotr.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Connor
 * @create 2021-06-29-19:56
 */
@Mapper
public interface UserMapper {

    User selectUserById(int id);

    User selectUserByName(String username);

    int insertUser(User user);

    int updatePasswordById(int id, String password);

    int updateHeaderUrlById(int id, String headerUrl);

}
