package com.nw.websocket.connect;

import org.springframework.web.socket.WebSocketSession;

/**
 * Websocket连接管理接口，用于处理和管理Websocket会话
 */
public interface WebsocketSessionManage {

    /**
     * 将特定用户的设备会话添加到管理中
     *
     * @param session Websocket会话对象
     * @param userId  用户ID
     * @param device  设备标识
     */
    void put(WebSocketSession session, String userId, String device);

    /**
     * 删除指定用户的设备会话
     *
     * @param session
     * @param userId  用户ID
     * @param device  设备标识
     */
    void delete(WebSocketSession session, String userId, String device);

    /**
     * 向指定用户的设备发送消息
     *
     * @param userId 用户ID
     * @param device 设备标识
     * @param message 消息内容
     */
    void sendMessage(String userId, String device, String message);

    /**
     * 向指定用户的所有设备发送消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    void sendMessage(String userId, String message);

    /**
     * 更新特定用户的设备会话的心跳时间，以保持会话活跃
     *
     * @param session Websocket会话对象
     * @param userId 用户ID
     * @param device 设备标识
     */
    void heartbeat(WebSocketSession session,String userId, String device);
}
