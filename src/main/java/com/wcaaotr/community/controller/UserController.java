package com.wcaaotr.community.controller;

import com.github.pagehelper.PageInfo;
import com.wcaaotr.community.Event.EventProducer;
import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.Util.CommunityUtil;
import com.wcaaotr.community.Util.HostHolder;
import com.wcaaotr.community.Util.RedisPageUtil;
import com.wcaaotr.community.entity.Event;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.FollowService;
import com.wcaaotr.community.service.LikeService;
import com.wcaaotr.community.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Connor
 * @create 2021-07-01-21:49
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getUserSettingPage() {
        return "/site/user-setting";
    }

    @RequestMapping(path = "/main/{userId}", method = RequestMethod.GET)
    public String getUserMainPage(@PathVariable("userId")int userId, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        int likeCount = likeService.findUserGetLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        boolean hasFollowed = false;
        User loginUser = hostHolder.getUser();
        if(loginUser != null) {
            hasFollowed = followService.hasUserFollowEntity(loginUser.getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/user-main";
    }

    @RequestMapping(path = "/uploadHeadImage", method = RequestMethod.POST)
    public String uploadHeadImage(@RequestParam("headImage") MultipartFile headImage, Model model) {
        String filename = headImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(!(".png".equals(suffix) || ".jpg".equals(suffix))) {
            model.addAttribute("uploadError", "文件格式错误！");
            return "/site/user-setting";
        }
        filename = CommunityUtil.generateUUID() + suffix;
        File localImage = new File(uploadPath + "/" + filename);
        try {
            headImage.transferTo(localImage);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生错误！");
        }
        User user = hostHolder.getUser();
        userService.updateHeaderUrlById(user.getId(), domain +  contextPath + "/user/getHeadImage/" + filename);
        return "redirect:/index";
    }

    @RequestMapping(path = "/getHeadImage/{filename}", method = RequestMethod.GET)
    public void getHeadImage(@PathVariable("filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        String suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/"+suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取图像失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model, @CookieValue("token") String token) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePasswordById(user.getId(), oldPassword, newPassword);
        if(map.containsKey("passwordError")) {
            model.addAttribute("passwordError", map.get("passwordError"));
            return "/site/user-setting";
        }
        userService.logout(token);
        return "redirect:/login";
    }

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    @RequestMapping(path = "/followerList/{userId}", method = RequestMethod.GET)
    public String getFollowerList(@PathVariable("userId")int userId, Model model,
                                  @RequestParam(defaultValue = "1", name = "pageNum")int pageNum) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        int rows = (int) followService.findFollowerCount(userId, ENTITY_TYPE_USER);
        PageInfo pageInfo = RedisPageUtil.getPageInfo(rows, 5, pageNum);
        model.addAttribute("pageInfo", pageInfo);

        List<Map<String, Object>> followers = followService.findAllFollowers(userId, pageNum, 5);
        if(followers != null) {
            for (Map<String, Object> map : followers) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followers", followers);

        return "/site/user-follower";
    }

    @RequestMapping(path = "/followeeList/{userId}", method = RequestMethod.GET)
    public String getFolloweeList(@PathVariable("userId")int userId, Model model,
                                  @RequestParam(defaultValue = "1", name = "pageNum")int pageNum) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        int rows = (int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        PageInfo pageInfo = RedisPageUtil.getPageInfo(rows, 5, pageNum);
        model.addAttribute("pageInfo", pageInfo);

        List<Map<String, Object>> followers = followService.findAllFollowees(userId, pageNum, 5);
        if(followers != null) {
            for (Map<String, Object> map : followers) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followees", followers);

        return "/site/user-followee";
    }

    public boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasUserFollowEntity(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }


}
