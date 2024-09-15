package com.nw.im.connect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private final WebsocketSessionManage websocketSessionManage;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.put(session, userId, device);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.delete(userId, device);
        log.debug("关闭与UserID：{},device：{}的连接", userId, device);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.delete(userId, device);
        log.debug("关闭与UserID：{},device：{}的连接", userId, device);
    }

    private static String getUserId(WebSocketSession session) {
        return session.getAttributes().get("userId").toString();
    }

    private static String getDevice(WebSocketSession session) {
        return session.getAttributes().get("device").toString();
    }
}
