package com.wcaaotr.community.interceptor;

import com.wcaaotr.community.Util.HostHolder;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Connor
 * @create 2021-07-04-22:46
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null) {
            int letterUnreadCount = messageService.findUnreadLetterCountByConversationId(null, user.getId());
            int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId());
            modelAndView.addObject("allUnreadCount", letterUnreadCount+noticeUnreadCount);
        }
    }
}
