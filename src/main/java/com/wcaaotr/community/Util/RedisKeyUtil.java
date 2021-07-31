package com.wcaaotr.community.Util;

/**
 * @author Connor
 * @create 2021-07-03-16:17
 */
public class RedisKeyUtil implements CommunityConstant {

    // like:comment:101 -> set(userId) 实体 post/comment/user/ 被哪个用户点赞了
    public static String getEntityLikeKey(int entityType, int entityId) {
        return "like:" + getType(entityType) + ":" + entityId;
    }

    // like:user:userId -> int  用户 userId 获得了多少个咱
    public static String getUserLikeKey(int userId) {
        return "like:user:" + userId;
    }

    //followee:user:userId:entitytype -> zset(entityId, now)  用户 Id 关注了 用户、帖子等
    public static String getUserFolloweeKey(int userId, int entityType) {
        return "followee:user:" + userId + ":" + getType(entityType);
    }

    //follower:entityType:entityId -> zset(userId, now)  实体 post 的粉丝 userId
    public static String getEntityFollowerKey(int entityType, int entityId) {
        return "follower:" + getType(entityType) + ":" + entityId;
    }

    private static String getType(int entityType) {
        String type = null;
        if(entityType == ENTITY_TYPE_POST){
            type = "post";
        } else if (entityType == ENTITY_TYPE_COMMENT) {
            type = "comment";
        } else if (entityType == ENTITY_TYPE_USER) {
            type = "user";
        }
        return type;
    }

    //token:asjlkdhflaskjdhf -> user
    public static String getTokenKey(String token) {
        return "token:" + token;
    }

    public static String getUserKey(int userId) {
        return "user:" + userId;
    }

    // unique visitor key 记录单日的独立访客
    public static String getUVkey(String date){
        return "uv:" + date;
    }

    // 区间 uv
    public static String getUVkey(String startDate, String endDate){
        return "uv:" + startDate + ":" + endDate;
    }

    public static String getDAUkey(String date){
        return "dau:" + date;
    }

    public static String getDAUkey(String startDate, String endDate){
        return "dau:" + startDate + ":" + endDate;
    }

    // 帖子热度，操作导致帖子分数发生变化的时候记录帖子的id，等待定时任务处理
    public static String getPostScoreChangeKey(){
        return "post:scoreChange";
    }

}
