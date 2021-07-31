package com.wcaaotr.community.dao;

import com.wcaaotr.community.CommunitydemoApplication;
import com.wcaaotr.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author Connor
 * @create 2021-07-02-22:39
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunitydemoApplication.class)
public class MessageMapperTests {
    @Autowired(required = false)
    private MessageMapper messageMapper;

    @Test
    public void testSelectLetterCountByConversationId() {
        int count = messageMapper.selectLetterCountByConversationId("111_112");
        System.out.println(count);
    }

    @Test
    public void testSelectUnreadLetterCountByConversationId() {
        int count = messageMapper.selectUnreadLetterCountByConversationId(null, 111);
        System.out.println(count);
    }

    @Test
    public void testInsertMessage() {
        Message letter = new Message();
        letter.setFromId(111);
        letter.setToId(112);
        if(letter.getFromId() < letter.getToId()) {
            letter.setConversationId(letter.getFromId() + "_" + letter.getToId());
        } else {
            letter.setConversationId(letter.getToId() + "_" + letter.getFromId());
        }
        letter.setContent("564123645");
        letter.setStatus(0);
        letter.setCreateTime(new Date());
        messageMapper.insertMessage(letter);
    }
}
