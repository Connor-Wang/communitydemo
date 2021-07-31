package com.wcaaotr.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.Util.CommunityUtil;
import com.wcaaotr.community.Util.HostHolder;
import com.wcaaotr.community.entity.Message;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.MessageService;
import com.wcaaotr.community.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author Connor
 * @create 2021-07-02-21:43
 */
@Controller
@RequestMapping("/message")
public class MessageController implements CommunityConstant{

    private static Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/conversationList", method = RequestMethod.GET)
    public String getConversationListPage(Model model, @RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum) {
        logger.warn("MessageController -> getConversationList -> in method");
        User user = hostHolder.getUser();
        logger.warn("MessageController -> getConversationList -> get user");

        PageHelper.startPage(pageNum, 10);
        List<Message> conversations = messageService.findConversationsByUserId(user.getId());
        logger.warn("MessageController -> getConversationList -> get conversations");
        PageInfo<Message> pageInfo = new PageInfo<>(conversations);
        model.addAttribute("pageInfo", pageInfo);

        logger.warn("MessageController -> getConversationList -> load page info");

        List<Map<String, Object>> conversationList = new ArrayList<>();
        if(conversations != null) {
            for (Message conversation : conversations) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", conversation);
                int targetId = user.getId().equals(conversation.getFromId()) ? conversation.getToId() : conversation.getFromId();
                map.put("targetUser", userService.findUserById(targetId));
                int letterCount = messageService.findLetterCountByConversationId(conversation.getConversationId());
                map.put("letterCount", letterCount);
                int unreadLetterCount = messageService.findUnreadLetterCountByConversationId(conversation.getConversationId(), user.getId());
                map.put("unreadLetterCount", unreadLetterCount);
                conversationList.add(map);
            }
        }
        model.addAttribute("conversations", conversationList);

        int unreadLetterCount = messageService.findUnreadLetterCountByConversationId(null, user.getId());
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId());
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        logger.warn("MessageController -> getConversationList -> code before return");
        return "/site/conversation";
    }

    @RequestMapping(path = "/conversationDetail/{conversationId}", method = RequestMethod.GET)
    public String getConversationDetailPage(@PathVariable("conversationId")String conversationId, Model model,
                                            @RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum) {
        PageHelper.startPage(pageNum, 5);
        List<Message> letters = messageService.findLettersByConversationId(conversationId);
        PageInfo<Message> pageInfo = new PageInfo<>(letters);
        model.addAttribute("pageInfo", pageInfo);

        List<Map<String, Object>> letterList = new ArrayList<>();
        // 记录未读的私信的 id
        List<Integer> unreadIds = new ArrayList<>();
        int userId = hostHolder.getUser().getId();
        if(letters != null) {
            for (Message letter : letters) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                letterList.add(map);
                if(userId == letter.getToId() && letter.getStatus() == 0){
                    unreadIds.add(letter.getId());
                }
            }
        }
        model.addAttribute("letterList", letterList);

        model.addAttribute("targetUser", getLetterTargetUser(conversationId));
        // 将该页的私信设置已读
        if(unreadIds != null && unreadIds.size() > 0){
            messageService.readMessageStatus(unreadIds);
        }

        return "/site/conversation-detail";
    }

    public User getLetterTargetUser(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "/sendLetter", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        User targetUser = userService.findUserByName(toName);
        if(targetUser == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        Message letter = new Message();
        letter.setFromId(hostHolder.getUser().getId());
        letter.setToId(targetUser.getId());
        if(letter.getFromId() < letter.getToId()) {
            letter.setConversationId(letter.getFromId() + "_" + letter.getToId());
        } else {
            letter.setConversationId(letter.getToId() + "_" + letter.getFromId());
        }
        letter.setContent(content);
        letter.setStatus(0);
        letter.setCreateTime(new Date());
        messageService.addMessage(letter);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/noticeList", method = RequestMethod.GET)
    public String getNoticeListPage(Model model) {
        User user = hostHolder.getUser();
        // 查询评论类通知
        Message message = messageService.findLatestNoticeByTopic(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            int count = messageService.findNoticeCountByTopic(user.getId(), TOPIC_COMMENT);
            int unread = messageService.findUnreadNoticeCountByTopic(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            messageVO.put("unread", unread);
        }
        model.addAttribute("commentNotice", messageVO);
        // 查询点赞类通知
        message = messageService.findLatestNoticeByTopic(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            int count = messageService.findNoticeCountByTopic(user.getId(), TOPIC_LIKE);
            int unread = messageService.findUnreadNoticeCountByTopic(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            messageVO.put("unread", unread);
        }
        model.addAttribute("likeNotice", messageVO);
        // 查询关注类通知
        message = messageService.findLatestNoticeByTopic(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            int count = messageService.findNoticeCountByTopic(user.getId(), TOPIC_FOLLOW);
            int unread = messageService.findUnreadNoticeCountByTopic(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            messageVO.put("unread", unread);
        }
        model.addAttribute("followNotice", messageVO);

        //查询未读消息数量
        int letterUnreadCount = messageService.findUnreadLetterCountByConversationId(null, user.getId());
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId());
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/noticeDetail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetailPage(@PathVariable("topic") String topic, Model model,
                                      @RequestParam(defaultValue = "1", value = "pageNum") int pageNum) {
        User user = hostHolder.getUser();

        PageHelper.startPage(pageNum, 5);
        List<Message> noticeList = messageService.findNoticeByTopic(user.getId(), topic);
        PageInfo<Message> pageInfo = new PageInfo<Message>(noticeList);
        model.addAttribute("pageInfo", pageInfo);

        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> unreadIds = new ArrayList<>();
        int userId = hostHolder.getUser().getId();
        if(noticeList != null) {
            for (Message notice : noticeList) {
                if(userId == notice.getToId() && notice.getStatus() == 0){
                    unreadIds.add(notice.getId());
                }
            }
        }
        if(!unreadIds.isEmpty()) {
            messageService.readMessageStatus(unreadIds);
        }

        return "/site/notice-detail";

    }
}
