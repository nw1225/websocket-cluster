package com.nw.im.common;

/**
 * 消息监听器接口
 * 用于处理接收到的消息
 */
public interface MessageListener {

    /**
     * 当消息被接收时调用的方法
     *
     * @param message 接收到的消息对象
     */
    void message(Message message);
}
