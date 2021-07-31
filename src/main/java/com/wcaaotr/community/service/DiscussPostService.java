package com.wcaaotr.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wcaaotr.community.dao.DiscussPostMapper;
import com.wcaaotr.community.entity.DiscussPost;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Connor
 * @create 2021-06-29-20:18
 */
@Service
public class DiscussPostService {

    private static Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expiteSeconds;

    // Caffeine 核心接口： Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    @PostConstruct
    public void initCache(){
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expiteSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        // 二级缓存 读取 redis
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(1);
                    }
                });
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public List<DiscussPost> findDiscussPosts(int orderMode, int pageNum) {
        if(orderMode == 1){
            return postListCache.get("postPage:"+pageNum);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(orderMode);
    }

    public int addDiscussPost(DiscussPost post) {
        return discussPostMapper.insertDiscussPost(post);
    }

    public int updateCommentCount(int id, int count) {
        return discussPostMapper.updateCommentCount(id, count);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
