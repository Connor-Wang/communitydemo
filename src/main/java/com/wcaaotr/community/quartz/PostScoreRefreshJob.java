package com.wcaaotr.community.quartz;

import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.Util.RedisKeyUtil;
import com.wcaaotr.community.entity.DiscussPost;
import com.wcaaotr.community.service.DiscussPostService;
import com.wcaaotr.community.service.LikeService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Connor
 * @create 2021-07-14-14:58
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    // 论坛初始时间
    private static final Date startDate;

    static {
        try {
            startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-1-1 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化时间失败！", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreChangeKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0){
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while(operations.size() > 0) {
            int i = (Integer) operations.pop();
            this.refresh(i);
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if(post == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }

        boolean great = post.getStatus() == 1;
        int commentCount = post.getCommentCount();
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        double weight = (great ? 75 : 0) + commentCount * 10 + likeCount * 2;
        double score = Math.log10(Math.max(weight, 1)) +
                (post.getCreateTime().getTime()-startDate.getTime())/(1000*3600*24);

        // 更新帖子的分数
        discussPostService.updateScore(postId, score);
    }
}
