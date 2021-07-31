package com.wcaaotr.community.interceptor;

import com.wcaaotr.community.Util.HostHolder;
import com.wcaaotr.community.entity.LoginToken;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Connor
 * @create 2021-06-30-22:17
 */
@Component
public class LoginTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("token")) {
                    token = cookie.getValue();
                }
            }
        }
        if(token != null) {
            LoginToken loginToken = userService.findLoginTokenByToken(token);
            if(loginToken != null && loginToken.getStatus() == 0 && loginToken.getExpired().after(new Date())) {
                User user = userService.findUserById(loginToken.getUserId());
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.removeUser();
    }
}
