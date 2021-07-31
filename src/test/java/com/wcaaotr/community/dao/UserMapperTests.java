package com.wcaaotr.community.dao;

import com.wcaaotr.community.CommunitydemoApplication;
import com.wcaaotr.community.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-06-29-20:29
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunitydemoApplication.class)
public class UserMapperTests {
    @Autowired(required = false)
    private UserMapper userMapper;
    private User data;

    @Before
    public void before() {
        data = new User();
        data.setUsername("Tom");
        data.setPassword("123123");
        data.setSalt("asd");
        data.setEmail("123@qq.com");
        data.setType(0);
        data.setHeaderUrl("asdsad.asd");
        data.setCreateTime(new Date());
    }

    @Test
    public void testSelectUserById() {
        User user = userMapper.selectUserById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectUserByName() {
        User user = userMapper.selectUserByName("Tom");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        userMapper.insertUser(data);
    }
}
