package com.nw.im.connect;

import com.nw.im.common.Constants;
import io.netty.util.HashedWheelTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class RedisWebsocketSessionManage implements WebsocketSessionManage {
    private static final Map<String, ConcurrentHashMap<String, WebSocketSession>> sessionPool = new ConcurrentHashMap<>();

    private final RedisTemplate<String, String> redisTemplate;
    private final HashedWheelTimer timer = new HashedWheelTimer(1, TimeUnit.SECONDS, 1024 * 1024);
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(200, 200, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());


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
            setRedisKey(userId, device);
            log.debug("建立与UserID：{},device：{}的连接", userId, device);
            return deviceMap;
        });
    }

    @Override
    public void delete(String userId, String device) {
        sessionPool.computeIfPresent(userId, (key, deviceMap) -> {
            deviceMap.remove(device);
            //从redis移除
            String redisKey = String.format(Constants.clientKeyPrefix, userId, device);
            redisTemplate.opsForValue().getAndDelete(redisKey);
            return deviceMap.isEmpty() ? null : deviceMap;
        });
    }

    @Override
    public void heartbeat(WebSocketSession session, String userId, String device) {
        threadPoolExecutor.execute(() -> {
            timer.newTimeout((task) -> {
                setRedisKey(userId, device);
                session.sendMessage(new PingMessage());
            }, 30, TimeUnit.SECONDS);
        });
    }


    private void setRedisKey(String userId, String device) {
        String redisKey = String.format(Constants.clientKeyPrefix, userId, device);
        redisTemplate.opsForValue().set(redisKey, Constants.nodeId, 60, TimeUnit.SECONDS);
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
