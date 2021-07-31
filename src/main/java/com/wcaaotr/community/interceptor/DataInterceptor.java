package com.wcaaotr.community.interceptor;

import com.wcaaotr.community.Util.HostHolder;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 每次访问记录 UV 和 DAU
 * @author Connor
 * @create 2021-07-13-20:20
 */
@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计 UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        // 统计 DAU
        User user = hostHolder.getUser();
        if(user != null) {
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
