package com.wcaaotr.community.dao;

import com.wcaaotr.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Connor
 * @create 2021-07-02-21:29
 */
@Mapper
public interface MessageMapper {

    // 查询某个用户 的 所有会话的最近一条私信
    List<Message> selectConversationsByUserId(int userId);

    int selectLetterCountByConversationId(String conversationId);

    int selectUnreadLetterCountByConversationId(String conversationId, int userId);

    List<Message> selectLettersByConversationId(String conversationId);

    int insertMessage(Message message);

    int updateMessageStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLatestNoticeByTopic(int userId, String topic);

    int selectNoticeCountByTopic(int userId, String topic);

    int selectUnreadNoticeCountByTopic(int userId, String topic);

    int selectUnreadNoticeCount(int userId);

    List<Message> selectNoticeByTopic(int userId, String topic);


}
