package com.wcaaotr.community.Util;

import com.wcaaotr.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author Connor
 * @create 2021-06-30-22:48
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void removeUser() {
        users.remove();
    }
}
