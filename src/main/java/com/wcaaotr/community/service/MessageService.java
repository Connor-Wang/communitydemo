package com.wcaaotr.community.service;

import com.wcaaotr.community.dao.MessageMapper;
import com.wcaaotr.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Connor
 * @create 2021-07-02-21:41
 */
@Service
public class MessageService {
    @Autowired(required = false)
    private MessageMapper messageMapper;

    public List<Message> findConversationsByUserId(int userId) {
        return messageMapper.selectConversationsByUserId(userId);
    }

    public int findLetterCountByConversationId(String conversationId) {
        return messageMapper.selectLetterCountByConversationId(conversationId);
    }

    public int findUnreadLetterCountByConversationId(String conversationId, int userId) {
        return messageMapper.selectUnreadLetterCountByConversationId(conversationId, userId);
    }

    public List<Message> findLettersByConversationId(String conversationId) {
        return messageMapper.selectLettersByConversationId(conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessageStatus(List<Integer> ids) {
        return messageMapper.updateMessageStatus(ids, 1);
    }

    public Message findLatestNoticeByTopic(int userId, String topic) {
        return messageMapper.selectLatestNoticeByTopic(userId, topic);
    }

    public int findNoticeCountByTopic(int userId, String topic) {
        return messageMapper.selectNoticeCountByTopic(userId, topic);
    }

    public int findUnreadNoticeCountByTopic(int userId, String topic) {
        return messageMapper.selectUnreadNoticeCountByTopic(userId, topic);
    }

    public int findUnreadNoticeCount(int userId) {
        return messageMapper.selectUnreadNoticeCount(userId);
    }

    public List<Message> findNoticeByTopic(int userId, String topic) {
        return messageMapper.selectNoticeByTopic(userId, topic);
    }

}
