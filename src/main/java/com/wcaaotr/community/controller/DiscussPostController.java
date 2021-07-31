package com.wcaaotr.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wcaaotr.community.Event.EventProducer;
import com.wcaaotr.community.Util.CommunityConstant;
import com.wcaaotr.community.Util.CommunityUtil;
import com.wcaaotr.community.Util.HostHolder;
import com.wcaaotr.community.Util.RedisKeyUtil;
import com.wcaaotr.community.entity.Comment;
import com.wcaaotr.community.entity.DiscussPost;
import com.wcaaotr.community.entity.Event;
import com.wcaaotr.community.entity.User;
import com.wcaaotr.community.service.CommentService;
import com.wcaaotr.community.service.DiscussPostService;
import com.wcaaotr.community.service.LikeService;
import com.wcaaotr.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Connor
 * @create 2021-07-02-14:17
 */
@Controller
@RequestMapping("/discusspost")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "请先登录！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setScore(0.0);
        discussPostService.addDiscussPost(discussPost);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreChangeKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "帖子发布成功");
    }

    @RequestMapping(path = "/detail/{postId}", method = RequestMethod.GET)
    public String getDiscussPostDetailPage(@PathVariable("postId")int postId, Model model,
                                           @RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post", post);
        User postUser = userService.findUserById(post.getUserId());
        model.addAttribute("postUser", postUser);
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        model.addAttribute("likeCount", likeCount);
        User loginUser = hostHolder.getUser();
        int likeStatus = loginUser == null ? 0 : likeService.findEntityLikeStatus(loginUser.getId(), ENTITY_TYPE_POST, postId);
        model.addAttribute("likeStatus", likeStatus);

        PageHelper.startPage(pageNum, 10);
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId());
        PageInfo<Comment> pageInfo = new PageInfo<>(commentList);
        model.addAttribute("pageInfo", pageInfo);

        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                User user = userService.findUserById(comment.getUserId());
                commentVo.put("user", user);
                commentVo.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
                commentVo.put("likeStatus", loginUser == null ? 0 : likeService.findEntityLikeStatus(loginUser.getId(), ENTITY_TYPE_COMMENT, comment.getId()));

                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        user = userService.findUserById(reply.getUserId());
                        replyVo.put("user", user);
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("targetUser", targetUser);
                        replyVo.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId()));
                        replyVo.put("likeStatus", loginUser == null ? 0 : likeService.findEntityLikeStatus(loginUser.getId(), ENTITY_TYPE_COMMENT, reply.getId()));
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                commentVo.put("replyCount", commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId()));
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discusspost-detail";
    }

    @RequestMapping(path = "/addComment/{postId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("postId")int postId, Comment comment) {
        User user = hostHolder.getUser();
        comment.setUserId(user.getId());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(user.getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreChangeKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);



        return "redirect:/discusspost/detail/" + postId;
    }

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String likeEntity(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        likeService.likeEntity(user.getId(), entityType, entityId, entityUserId);
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if(likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if(entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreChangeKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

    @RequestMapping(path = "/setTop", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/setGreat", method = RequestMethod.POST)
    @ResponseBody
    public String setGreat(int id) {
        discussPostService.updateStatus(id, 1);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreChangeKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/setDelete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        return CommunityUtil.getJSONString(0);
    }

}
