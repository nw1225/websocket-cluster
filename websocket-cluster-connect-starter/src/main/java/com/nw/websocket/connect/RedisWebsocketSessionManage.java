package com.nw.websocket.connect;

import com.nw.websocket.common.Constants;
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

/**
 * 基于Redis的WebSocket会话管理实现类
 */
@Slf4j
@RequiredArgsConstructor
public class RedisWebsocketSessionManage implements WebsocketSessionManage {
    // 会话池，用于存储所有用户的WebSocket会话
    private static final Map<String, ConcurrentHashMap<String, WebSocketSession>> sessionPool = new ConcurrentHashMap<>();

    private final RedisTemplate<String, String> redisTemplate;
    // 线程池，用于处理异步任务
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(200, 200, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());
    // 定时任务定时器，用于处理心跳等定时任务
    private final HashedWheelTimer timer = new HashedWheelTimer(1, TimeUnit.SECONDS, 1024 * 1024);

    /**
     * 将WebSocket会话添加到会话池
     * 如果用户已存在相同设备的会话，则关闭旧会话并添加新会话
     *
     * @param session WebSocket会话
     * @param userId  用户ID
     * @param device  设备标识
     */
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

    /**
     * 从会话池中删除指定用户的WebSocket会话
     *
     * @param userId 用户ID
     * @param device 设备标识
     */
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

    /**
     * 处理会心跳消息，更新Redis中的会话状态
     *
     * @param session WebSocket会话
     * @param userId  用户ID
     * @param device  设备标识
     */
    @Override
    public void heartbeat(WebSocketSession session, String userId, String device) {
        threadPoolExecutor.execute(() -> timer.newTimeout((task) -> {
            setRedisKey(userId, device);
            session.sendMessage(new PingMessage());
        }, 30, TimeUnit.SECONDS));
    }

    /**
     * 在Redis中设置会话键，用于会话管理和心跳检测
     *
     * @param userId 用户ID
     * @param device 设备标识
     */
    private void setRedisKey(String userId, String device) {
        String redisKey = String.format(Constants.clientKeyPrefix, userId, device);
        redisTemplate.opsForValue().set(redisKey, Constants.nodeId, 60, TimeUnit.SECONDS);
    }

    /**
     * 向指定用户和设备发送消息
     *
     * @param userId  用户ID
     * @param device  设备标识
     * @param message 消息内容
     */
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

    /**
     * 向指定用户的所有设备发送消息
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
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
