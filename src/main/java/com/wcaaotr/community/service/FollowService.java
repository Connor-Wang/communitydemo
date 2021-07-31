package com.wcaaotr.community.service;

import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.Util.CommunityUtil;
import com.wcaaotr.community.Util.RedisKeyUtil;
import com.wcaaotr.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Connor
 * @create 2021-07-04-16:09
 */
@Service
public class FollowService implements CommunityConstant{

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
                String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);

                operations.multi();
                redisTemplate.opsForZSet().add(userFolloweeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(entityFollowerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
                String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);

                operations.multi();
                redisTemplate.opsForZSet().remove(userFolloweeKey, entityId);
                redisTemplate.opsForZSet().remove(entityFollowerKey, userId);

                return operations.exec();
            }
        });
    }

    public long findFolloweeCount(int userId, int entityType) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(userFolloweeKey);
    }

    public long findFollowerCount(int entityType, int entityId) {
        String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(entityFollowerKey);
    }

    // 查询当前用户是否关注该实体
    public boolean hasUserFollowEntity(int userId, int entityType, int entityId) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        Double score = redisTemplate.opsForZSet().score(userFolloweeKey, entityId);
        return score != null;
    }

    public List<Map<String, Object>> findAllFollowers(int userId, int pageNum, int pageSize) {
        String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(entityFollowerKey, (pageNum - 1) * pageSize, pageNum * pageSize - 1);
        if(set == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : set) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(entityFollowerKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> findAllFollowees(int userId, int pageNum, int pageSize) {
        String entityFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(entityFolloweeKey, (pageNum - 1) * pageSize, pageNum * pageSize - 1);
        if(set == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : set) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(entityFolloweeKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
