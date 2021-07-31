package com.wcaaotr.community.service;

import com.wcaaotr.community.Util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 统计数据
 * @author Connor
 * @create 2021-07-13-20:03
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定的 ip 计入 UV
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVkey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的 UV
    public long calculateUV(Date start, Date end){
        if(start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVkey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        String rediKey = RedisKeyUtil.getUVkey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(rediKey, keyList.toArray());

        return redisTemplate.opsForHyperLogLog().size(rediKey);
    }

    // 将指定的 ip 计入 DAU
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUkey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的 DAU
    public long calculateDAU(Date start, Date end){
        if(start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            byte[] key = RedisKeyUtil.getDAUkey(df.format(calendar.getTime())).getBytes();
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String rediKey = RedisKeyUtil.getDAUkey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR, rediKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(rediKey.getBytes());
            }
        });
    }
}
