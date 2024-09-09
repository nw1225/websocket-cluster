package com.nw.im.connect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Map<String, ConcurrentHashMap<String, WebSocketSession>> sessionPool = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        String device = getDevice(session);
        sessionPool.compute(userId, (key, map) -> {
            //todo 注册到redis
            ConcurrentHashMap<String, WebSocketSession> deviceMap = map != null ? map : new ConcurrentHashMap<>();
            WebSocketSession oldSession = deviceMap.get(device);
            if (Objects.nonNull(oldSession) && !oldSession.equals(session)) {
                try {
                    log.info("UserID：{},device：{}的连接已存在", userId, device);
                    oldSession.close();
                } catch (IOException e) {
                    log.warn("关闭旧会话时发生异常", e);
                }
            }
            deviceMap.put(device, session);
            log.info("建立与UserID：{},device：{}的连接", userId, device);
            return deviceMap;
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        String device = getDevice(session);
        sessionPool.computeIfPresent(userId, (key, deviceMap) -> {
            //todo 从redis移除
            deviceMap.remove(device);
            return deviceMap.isEmpty() ? null : deviceMap;
        });
        log.info("关闭与UserID：{},device：{}的连接", userId, device);

    }

    private static String getUserId(WebSocketSession session) {
        return session.getAttributes().get("userId").toString();
    }

    private static String getDevice(WebSocketSession session) {
        return session.getAttributes().get("device").toString();
    }
}
