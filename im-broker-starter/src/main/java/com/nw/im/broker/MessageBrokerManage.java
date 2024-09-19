package com.nw.im.broker;

/**
 * 消息代理管理接口
 * 该接口定义了发送消息的功能，允许向指定用户或设备发送消息
 */
public interface MessageBrokerManage {

    /**
     * 向指定用户发送消息
     * 当需要向特定用户发送消息时使用此方法，消息将被发送到用户的全部设备
     *
     * @param userId  用户的唯一标识符
     * @param message 要发送的消息内容
     */
    void send(String userId, String message);

    /**
     * 向指定用户和设备发送消息
     * 当需要向特定用户发送消息，并且需要指定接收消息的设备时使用此方法
     *
     * @param userId 用户的唯一标识符
     * @param device 接收消息的设备标识符
     * @param message 要发送的消息内容
     */
    void send(String userId, String device, String message);
}
