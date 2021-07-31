package com.wcaaotr.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.entity.DiscussPost;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.DiscussPostService;
import com.wcaaotr.community.service.LikeService;
import com.wcaaotr.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author Connor
 * @create 2021-06-29-20:20
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String root() {
        return "forward:/index";
    }


    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, @RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum,
                               @RequestParam(defaultValue = "0", name = "orderMode") int orderMode) {
        PageHelper.startPage(pageNum, 10);
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(orderMode, pageNum);
        PageInfo<DiscussPost> pageInfo = new PageInfo<DiscussPost>(discussPosts);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("pagePath", "/index?orderMode=" + orderMode);

        List<Map<String, Object>> list = new ArrayList<>();
        if(discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                // 发帖用户
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                list.add(map);
            }
        }
        model.addAttribute("postList", list);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/message", method = RequestMethod.GET)
    public String getMessagePage() {
        return "/site/message";
    }

}
