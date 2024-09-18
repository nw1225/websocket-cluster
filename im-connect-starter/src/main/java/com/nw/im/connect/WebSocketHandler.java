package com.nw.im.connect;

import com.nw.im.common.Constants;
import io.netty.util.HashedWheelTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private final WebsocketSessionManage websocketSessionManage;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.put(session, userId, device);
        heartbeat(session);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectionClose(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        connectionClose(session);
    }

    private void connectionClose(WebSocketSession session) {
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.delete(userId, device);
        log.debug("关闭与UserID：{},device：{}的连接", userId, device);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        heartbeat(session);
    }

    private void heartbeat(WebSocketSession session) {
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.heartbeat(session,userId, device);
    }

    private static String getUserId(WebSocketSession session) {
        return session.getAttributes().get(Constants.userId).toString();
    }

    private static String getDevice(WebSocketSession session) {
        return session.getAttributes().get(Constants.device).toString();
    }
}
