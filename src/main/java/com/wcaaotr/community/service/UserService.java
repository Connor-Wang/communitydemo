package com.wcaaotr.community.service;

import com.wcaaotr.community.Util.CommunityUtil;
import com.wcaaotr.community.Util.RedisKeyUtil;
import com.wcaaotr.community.dao.LoginTokenMapper;
import com.wcaaotr.community.dao.UserMapper;
import com.wcaaotr.community.entity.LoginToken;
import com.wcaaotr.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Connor
 * @create 2021-06-29-20:31
 */
@Service
public class UserService {
    @Autowired(required = false)
    private UserMapper userMapper;
//    @Autowired(required = false)
//    private LoginTokenMapper loginTokenMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
        //return userMapper.selectUserById(id);
        User user = findUserFromCache(id);
        if(user == null) {
            user = findUserAndInitCache(id);
        }
        return user;
    }

    public User findUserByName(String username) {
        return userMapper.selectUserByName(username);
    }

    public Map<String, Object> register(User user) {
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        Map<String, Object> map = new HashMap<>();

        User userExist = userMapper.selectUserByName(user.getUsername());
        if(userExist != null) {
            map.put("usernameMsg", "该用户名已被注册！");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        return map;
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectUserByName(username);
        if(user == null) {
            map.put("usernameMsg", "账号不存在，请重新输入！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确，请重新输入！");
            return map;
        }

        LoginToken loginToken = new LoginToken();
        loginToken.setUserId(user.getId());
        loginToken.setToken(CommunityUtil.generateUUID());
        loginToken.setStatus(0); // 有效状态
        loginToken.setExpired(new Date(System.currentTimeMillis() + ((long)expiredSeconds) * 1000));

        //loginTokenMapper.insertLoginToken(loginToken);
        String tokenKey = RedisKeyUtil.getTokenKey(loginToken.getToken());
        redisTemplate.opsForValue().set(tokenKey, loginToken);

        map.put("token", loginToken.getToken());

        return map;
    }

    public void logout(String token) {
        //loginTokenMapper.updateLoginTokenStatus(token, 1);
        String tokenKey = RedisKeyUtil.getTokenKey(token);
        LoginToken loginToken = (LoginToken) redisTemplate.opsForValue().get(tokenKey);
        loginToken.setStatus(1);
        redisTemplate.opsForValue().set(tokenKey, loginToken);
    }

    public LoginToken findLoginTokenByToken(String token){
        //return loginTokenMapper.selectLoginTokenByToken(token);
        String tokenKey = RedisKeyUtil.getTokenKey(token);
        return (LoginToken) redisTemplate.opsForValue().get(tokenKey);
    }

    public int updateHeaderUrlById(int userId, String headerUrl) {
        int rows = userMapper.updateHeaderUrlById(userId, headerUrl);
        deleteUserFromCache(userId);
        return rows;
    }

    public Map<String, Object>  updatePasswordById(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectUserById(userId);
        String password = user.getPassword();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!password.equals(oldPassword)) {
            map.put("passwordError", "密码不正确，请重新输入！");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePasswordById(userId, newPassword);
        deleteUserFromCache(userId);
        return map;
    }

    public User findUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    public User findUserAndInitCache(int userId) {
        User user = userMapper.selectUserById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    public void deleteUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
