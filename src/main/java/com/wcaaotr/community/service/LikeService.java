package com.wcaaotr.community.service;

import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.Util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.security.auth.callback.Callback;

/**
 * @author Connor
 * @create 2021-07-03-16:16
 */
@Service
public class LikeService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    public void likeEntity(int userId, int entityType, int entityId, int entityUserId) {
        /*String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMemember = redisTemplate.opsForSet().isMember(likeEntityKey, userId);
        if(isMemember) {
            redisTemplate.opsForSet().remove(likeEntityKey, userId);
        } else {
            redisTemplate.opsForSet().add(likeEntityKey, userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String likeUserKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean isMemember = redisTemplate.opsForSet().isMember(likeEntityKey, userId);

                operations.multi();
                if(isMemember) {
                    redisTemplate.opsForSet().remove(likeEntityKey, userId);
                    redisTemplate.opsForValue().decrement(likeUserKey);
                } else {
                    redisTemplate.opsForSet().add(likeEntityKey, userId);
                    redisTemplate.opsForValue().increment(likeUserKey);
                }

                return operations.exec();
            }
        });
    }

    public long findEntityLikeCount(int entityType, int entityId) {
        String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(likeEntityKey);
    }

    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean isMemember = redisTemplate.opsForSet().isMember(likeEntityKey, userId);
        return isMemember ? 1 : 0;
    }

    public int findUserGetLikeCount(int userId) {
        String likeUserKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(likeUserKey);
        return count == null ? 0 : count;
    }
}
