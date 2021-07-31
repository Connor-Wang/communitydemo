package com.wcaaotr.community.dao;

import com.wcaaotr.community.CommunitydemoApplication;
import com.wcaaotr.community.entity.DiscussPost;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;


/**
 * @author Connor
 * @create 2021-06-29-20:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunitydemoApplication.class)
public class DiscussPostMapperTests {
    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;

    private DiscussPost data;

    @Before
    public void before(){
        data = new DiscussPost();
        data.setUserId(101);
        data.setTitle("Test");
        data.setContent("Test");
        data.setType(0);
        data.setStatus(0);
        data.setCreateTime(new Date());
        data.setCommentCount(0);
        data.setScore(0.0);
    }

    @After
    public void after(){}

    @Test
    public void testSelectDiscussPostByUserId(){
        DiscussPost discussPosts = discussPostMapper.selectDiscussPostById(101);
    }

    @Test
    public void testSelectDiscussPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0);
        Assert.assertEquals(168, discussPosts.size());
    }

    @Test
    public void testInsertDiscussPosts(){
        int rows = discussPostMapper.insertDiscussPost(data);
        Assert.assertEquals(1, rows);
    }


}
