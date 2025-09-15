package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.*;
import com.example.entity.vo.request.AddCommentVO;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.entity.vo.request.TopicUpdateVO;
import com.example.entity.vo.response.CommentVO;
import com.example.entity.vo.response.TopicDetailVO;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.entity.vo.response.TopicTopVO;
import com.example.mapper.*;
import com.example.service.NotificationService;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Resource
    TopicTypeMapper mapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    StringRedisTemplate template;

    @Resource
    TopicCommentMapper commentMapper;

    private Set<Integer> types = null;
    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    private void initTypes() {
        types = this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<TopicType> listTypes() {
        return mapper.selectList(null);
    }

    @Override
    public String createTopic(int uid, TopicCreateVO vo) {
        if (!textLimitCheck(vo.getContent(), 20000)) {
            return "Too much content. Fail to send";
        }
        if (!types.contains(vo.getType())) {
            return "Invalid topic type.";
        }
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if (!flowUtils.limitPeriodCounterCheck(key, 3, 3600)) {
            return "To frequent to send, please wait";
        }
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo, topic);
        topic.setContent(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if (this.save(topic)) {
            cacheUtils.deleteCache(Const.FORUM_TOPIC_CREATE_COUNTER + "*");
            return null;
        } else {
            return "Internal failure. Please contact administrator";
        }
    }

    private boolean textLimitCheck(JSONObject object, int max) {
        if (object == null) return false;

        long length = 0;
        JSONArray ops = object.getJSONArray("ops");
        if (ops == null) return false;

        for (Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            if (insert instanceof String s) {
                length += s.length();
                if (length > max) return false;
            }
        }
        return true;
    }

    @Override
    public List<TopicPreviewVO> listTopicByPage(int pageNumber, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + ":" + type;

        List<TopicPreviewVO> cached = cacheUtils.takeListFromCache(key, TopicPreviewVO.class);
        if (cached != null) return cached;

        // MyBatis-Plus 页码从 1 开始更稳
        long current = Math.max(1, pageNumber);
        Page<Topic> page = Page.of(current, 10);

        // 用 LambdaQueryWrapper 更不容易写错列名
        baseMapper.selectPage(
                page,
                Wrappers.<Topic>lambdaQuery()
                        .eq(type != 0, Topic::getType, type)
                        .orderByDesc(Topic::getTime)
        );

        List<Topic> topics = page.getRecords();
        if (topics == null || topics.isEmpty()) {
            List<TopicPreviewVO> empty = Collections.emptyList();
            cacheUtils.saveListToCache(key, empty, 60);  // 空结果也缓存，减少打库
            return empty;
        }

        // 保险：过滤 null，并兜底 resolveToPreview 里的潜在异常
        List<TopicPreviewVO> list = topics.stream()
                .filter(Objects::nonNull)
                .map(t -> {
                    try { return resolveToPreview(t); }
                    catch (Exception e) {
                        // 可按需打日志：log.warn("resolveToPreview fail, topicId={}", t.getId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        cacheUtils.saveListToCache(key, list, 60);
        return list;
    }


    private TopicPreviewVO resolveToPreview(Topic topic) {
        if (topic == null) return null;

        TopicPreviewVO vo = new TopicPreviewVO();

        // 先从 Topic 拷贝（source 一定非 null）
        BeanUtils.copyProperties(topic, vo);

        // 作者信息：可能查不到，判空后再拷或手动 set
        var account = accountMapper.selectById(topic.getUid());
        if (account != null) {
            // 建议只拷需要的字段，避免覆盖已填充的字段
            // BeanUtils.copyProperties(account, vo); // 若 VO 与 Account 字段大量同名且你确认不会覆盖不该覆盖的值，再用这一行
            vo.setUsername(account.getUsername());
            vo.setAvatar(account.getAvatar());
            // 其他你需要的字段按需 set ...
        }

        // 点赞/收藏计数，兜异常
        try {
            vo.setLike(baseMapper.interactCount(topic.getId(), "like"));
        } catch (Exception ignore) { vo.setLike(0); }
        try {
            vo.setCollect(baseMapper.interactCount(topic.getId(), "collect"));
        } catch (Exception ignore) { vo.setCollect(0); }

        // 预览文本 + 图片提取：容错处理（content 为空/非 JSON 都不崩）
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        String content = topic.getContent();
        if (content != null && !content.isBlank()) {
            try {
                var obj = JSONObject.parseObject(content);
                var ops = obj == null ? null : obj.getJSONArray("ops");
                if (ops != null) {
                    // 你已有的提取逻辑：只在 insert 为字符串时累计长度；图片走回调
                    this.shortContent(ops, previewText, o -> {
                        if (o != null) images.add(String.valueOf(o));
                    });
                } else {
                    // 没有 ops，当纯文本处理
                    previewText.append(content);
                }
            } catch (Exception e) {
                // 解析失败，当纯文本
                previewText.append(content);
            }
        }

        // 预览长度统一用 300
        String text = previewText.toString();
        vo.setText(text.length() > 300 ? text.substring(0, 300) : text);
        vo.setImages(images);

        return vo;
    }


    @Override
    public List<TopicTopVO> listTopTopics() {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query()
                .select("id", "title", "time")
                .eq("top", 1));
        return topics.stream().map(topic -> {
            TopicTopVO vo = new TopicTopVO();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }).toList();
    }

    @Override
    public TopicDetailVO getTopic(int tid, int uid) {
        TopicDetailVO vo = new TopicDetailVO();
        Topic topic = baseMapper.selectById(tid);
        if (topic == null) {
            return null;
        }
        BeanUtils.copyProperties(topic, vo);
        TopicDetailVO.Interact interact = new TopicDetailVO.Interact(
                hasInteract(tid, uid, "like"),
                hasInteract(tid, uid, "collect")
        );
        vo.setInteract(interact);
        TopicDetailVO.User user = new TopicDetailVO.User();
        if (topic.getUid() != null) {
            vo.setUser(this.fillUserDetailsByPrivacy(user, topic.getUid()));
        } else {
            vo.setUser(user); // Set empty user if uid is null
        }
        return vo;
    }

    private <T> T fillUserDetailsByPrivacy(T target, int uid) {
        AccountDetails details = accountDetailsMapper.selectById(uid);
        Account account = accountMapper.selectById(uid);
        AccountPrivacy accountPrivacy = accountPrivacyMapper.selectById(uid);
        String[] ignores = accountPrivacy.hiddenFields();
        BeanUtils.copyProperties(account, target, ignores);
        BeanUtils.copyProperties(details, target, ignores);
        return target;
    }

    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        synchronized (type.intern()) {
            template.opsForHash().put(type, interact.toKey(), Boolean.toString(state));
        }

    }

    private Boolean hasInteract(int tid, int uid, String type) {
        String key = tid + ":" + uid;
        if (template.opsForHash().hasKey(type, key)) {
            return Boolean.parseBoolean(template.opsForHash().entries(type).get(key).toString());
        }
        return baseMapper.userInteractCount(tid, uid, type) > 0;
    }

    private final Map<String, Boolean> state = new HashMap<>();
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

    private void saveInteractSchedule(String type) {
        if (!state.getOrDefault(type, true)) {
            state.put(type, true);
            service.schedule(() -> {
                this.saveInteract(type);
                state.put(type, false);
            }, 3, TimeUnit.SECONDS);
        }
    }

    private void saveInteract(String type) {
        synchronized (type.intern()) {
            List<Interact> check = new LinkedList<>();
            List<Interact> uncheck = new LinkedList<>();
            template.opsForHash().entries(type).forEach((k, v) -> {
                if (Boolean.parseBoolean(v.toString())) {
                    check.add(Interact.parseInteract(k.toString(), type));
                } else {
                    uncheck.add(Interact.parseInteract(k.toString(), type));
                }
            });
            if (!check.isEmpty()) {
                baseMapper.addInteract(check, type);
            }
            if (!uncheck.isEmpty()) {
                baseMapper.deleteInteract(uncheck, type);
            }
            template.delete(type);
        }
    }

    @Override
    public List<TopicPreviewVO> listTopicCollects(int uid) {
        return baseMapper.collectTopics(uid)
                .stream()
                .map(topic -> {
                    TopicPreviewVO vo = new TopicPreviewVO();
                    BeanUtils.copyProperties(topic, vo);
                    return vo;
                })
                .toList();
    }

    @Override
    public String updateTopic(int uid, TopicUpdateVO vo) {
        if (!textLimitCheck(vo.getContent(), 20000)) {
            return "Too much content. Fail to send";
        }
        if (!types.contains(vo.getType())) {
            return "Invalid topic type.";
        }
        baseMapper.update(null, Wrappers.<Topic>update()
                .eq("uid", uid)
                .eq("id", vo.getId())
                .set("title", vo.getTitle())
                .set("content", vo.getContent().toString())
                .set("type", vo.getType()));
        return null;
    }

    @Override
    public String creatComment(int uid, AddCommentVO vo) {
        if (!textLimitCheck(JSONObject.parseObject(vo.getContent()), 2000)) {
            return "Too much content. Fail to send";
        }
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if (!flowUtils.limitPeriodCounterCheck(key, 2, 60)) {
            return "Too many requests. Fail to send";
        }
        TopicComment comment = new TopicComment();
        comment.setUid(uid);
        BeanUtils.copyProperties(vo, comment);
        comment.setTime(new Date());
        commentMapper.insert(comment);
        Topic topic = baseMapper.selectById(vo.getTid());
        Account account = accountMapper.selectById(uid);
        if (vo.getQuote() > 0){
            TopicComment com = commentMapper.selectById(vo.getQuote());
            if (!Objects.equals(account.getId(), com.getUid())){
                notificationService.addNotification(com.getUid(),
                        "Someone reply to your comment",
                        account.getUsername() +
                        " replies to your comment",
                        "success", "/index/topic-detail/" + com.getId());
            }
        } else if (!Objects.equals(account.getId(), topic.getUid())) {
            notificationService.addNotification(topic.getUid(),
                    "Someone reply to your topic",
                    account.getUsername() +
                            " replies to your topic " +
                    topic.getTitle() ,
                    "success", "/index/topic-detail/" + topic.getId());
        }
        return null;
    }

    @Override
    public List<CommentVO> comments(int tid, int pageNumber) {
        Page<TopicComment> page = Page.of(pageNumber, 10);
        commentMapper.selectPage(page, Wrappers.<TopicComment>query()
                .eq("tid", tid));
        return page.getRecords().stream().map(dto -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(dto, vo);
            if (dto.getQuote() > 0) {
                TopicComment comment = commentMapper.selectOne(Wrappers.<TopicComment>query()
                        .eq("id", dto.getId()).orderByAsc("time"));
                if (comment != null) {
                    JSONObject object = JSONObject.parseObject(comment.getContent());
                    StringBuilder builder = new StringBuilder();
                    this.shortContent(object.getJSONArray("ops"), builder, ignore -> {
                    });
                    vo.setQuote(builder.toString());
                } else {
                    vo.setQuote("This comment has been deleted");
                }

            }
            CommentVO.User user = new CommentVO.User();
            this.fillUserDetailsByPrivacy(user, dto.getUid());
            vo.setUser(user);
            return vo;
        }).toList();
    }

    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object> imageHandler) {
        for (Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            if (insert instanceof String text) {
                if (previewText.length() > 300) {
                    continue;
                }
                previewText.append(text);
            } else if (insert instanceof Map<?, ?> map) {
                Optional.ofNullable(map.get("image")).ifPresent(imageHandler);
            }
        }
    }

    @Override
    public void deleteComment(int id, int uid) {
        commentMapper.delete(Wrappers.<TopicComment>query()
                .eq("id", id)
                .eq("uid", uid));
    }
}
