package com.wcaaotr.community;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wcaaotr.community.entity.DiscussPost;
import com.wcaaotr.community.service.DiscussPostService;
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
 * @create 2021-07-14-19:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunitydemoApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("本地缓存测试数据 -- 标题");
            post.setContent("本地缓存测试数据.本地缓存测试数据.本地缓存测试数据.本地缓存测试数据.本地缓存测试数据.");
            post.setType(0);
            post.setStatus(0);
            post.setCreateTime(new Date());
            post.setCommentCount(0);
            post.setScore(Math.random() * 2000);

            discussPostService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache() {
        PageHelper.startPage(1, 10);
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(1, 1);
        PageInfo<DiscussPost> pageInfo = new PageInfo<DiscussPost>(discussPosts);

        PageHelper.startPage(1, 10);
        List<DiscussPost> discussPosts2 = discussPostService.findDiscussPosts(1, 1);
        PageInfo<DiscussPost> pageInfo2 = new PageInfo<DiscussPost>(discussPosts);

        PageHelper.startPage(1, 10);
        List<DiscussPost> discussPosts3 = discussPostService.findDiscussPosts(1, 1);
        PageInfo<DiscussPost> pageInfo3 = new PageInfo<DiscussPost>(discussPosts);

        PageHelper.startPage(1, 10);
        List<DiscussPost> discussPosts4 = discussPostService.findDiscussPosts(1, 2);
        PageInfo<DiscussPost> pageInfo4 = new PageInfo<DiscussPost>(discussPosts);
    }
}
