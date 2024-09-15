package com.nw.im.connect;

import com.nw.im.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class RedisWebsocketSessionManage implements WebsocketSessionManage {
    private static final Map<String, ConcurrentHashMap<String, WebSocketSession>> sessionPool = new ConcurrentHashMap<>();

    private final RedisTemplate<String, ?> redisTemplate;


    @Override
    public void put(WebSocketSession session, String userId, String device) {
        sessionPool.compute(userId, (key, map) -> {
            ConcurrentHashMap<String, WebSocketSession> deviceMap = map != null ? map : new ConcurrentHashMap<>();
            WebSocketSession oldSession = deviceMap.get(device);
            if (Objects.nonNull(oldSession) && !oldSession.equals(session)) {
                try {
                    log.debug("UserID：{},device：{}的连接已存在", userId, device);
                    oldSession.close();
                } catch (IOException e) {
                    log.warn("关闭旧会话时发生异常", e);
                }
            }
            deviceMap.put(device, session);
            //注册到redis
            redisTemplate.opsForHash().put(Constants.clientKeyPrefix + userId, device, Constants.nodeId);
            log.debug("建立与UserID：{},device：{}的连接", userId, device);
            return deviceMap;
        });
    }

    @Override
    public void delete(String userId, String device) {
        sessionPool.computeIfPresent(userId, (key, deviceMap) -> {
            deviceMap.remove(device);
            //从redis移除
            redisTemplate.opsForHash().delete(Constants.clientKeyPrefix + userId, device);
            return deviceMap.isEmpty() ? null : deviceMap;
        });
    }

    @Override
    public void sendMessage(String userId, String device, String message) {
        Map<String, WebSocketSession> deviceMap = sessionPool.get(userId);
        if (Objects.nonNull(deviceMap)) {
            WebSocketSession session = deviceMap.get(device);
            if (Objects.nonNull(session)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.warn("消息推送失败：{} {}", userId, device, e);
                }
            }
        }
    }

    @Override
    public void sendMessage(String userId, String message) {
        Map<String, WebSocketSession> deviceMap = sessionPool.get(userId);
        if (Objects.nonNull(deviceMap)) {
            deviceMap.forEach((key, session) -> {
                if (Objects.nonNull(session)) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.warn("消息推送失败：{} {}", userId, key, e);
                    }
                }
            });
        }
    }
}
