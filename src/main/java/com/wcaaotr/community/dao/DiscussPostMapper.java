package com.wcaaotr.community.dao;

import com.wcaaotr.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Connor
 * @create 2021-06-29-19:50
 */
@Mapper
public interface DiscussPostMapper {

    DiscussPost selectDiscussPostById(int Id);

    List<DiscussPost> selectDiscussPosts(int orderMode);

    int insertDiscussPost(DiscussPost discussPost);

    int updateCommentCount(int id, int count);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
