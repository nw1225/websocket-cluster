package com.nw.websocket.connect;

import com.nw.websocket.common.Constants;
import com.nw.websocket.connect.config.WebsocketClusterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket处理器类，处理WebSocket连接的各种事件，如连接建立、关闭、错误以及心跳等
 */
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    // 注入WebSocket会话管理器，用于管理WebSocket会话
    private final WebsocketSessionManage websocketSessionManage;
    private final WebsocketClusterProperties websocketClusterProperties;

    /**
     * 从WebSocket会话中提取用户ID
     *
     * @param session WebSocket会话
     * @return 用户ID
     */
    private static String getUserId(WebSocketSession session) {
        return session.getAttributes().get(Constants.userId).toString();
    }

    /**
     * 从WebSocket会话中提取设备信息
     *
     * @param session WebSocket会话
     * @return 设备信息
     */
    private static String getDevice(WebSocketSession session) {
        return session.getAttributes().get(Constants.device).toString();
    }

    /**
     * 在WebSocket连接建立后调用
     *
     * @param session WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        // 获取用户ID和设备信息，将会话信息存入管理器，并启动心跳
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.put(session, userId, device);
        heartbeat(session);
    }

    /**
     * 在WebSocket连接关闭时调用
     *
     * @param session WebSocket会话
     * @param status  连接关闭状态
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        connectionClose(session);
    }

    /**
     * 处理WebSocket传输错误
     *
     * @param session   WebSocket会话
     * @param exception 异常
     */
    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        connectionClose(session);
    }

    /**
     * 关闭WebSocket连接，从会话管理器中移除相关信息
     *
     * @param session 要关闭的WebSocket会话
     */
    private void connectionClose(WebSocketSession session) {
        // 获取用户ID和设备信息，从会话管理器中删除对应会话
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.delete(userId, device);
        log.debug("关闭与UserID：{},device：{}的连接", userId, device);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String text = message.getPayload();
        String pingText = websocketClusterProperties.getPingText();
        if (pingText.equalsIgnoreCase(text)) {
            heartbeat(session);
            String pongText = websocketClusterProperties.getPongText();
            session.sendMessage(new TextMessage(pongText));
        }
    }

    /**
     * 发送心跳消息，保持WebSocket连接的活性
     *
     * @param session WebSocket会话
     */
    private void heartbeat(WebSocketSession session) {
        // 获取用户ID和设备信息，更新会话的心跳时间
        String userId = getUserId(session);
        String device = getDevice(session);
        websocketSessionManage.heartbeat(userId, device);
    }
}
