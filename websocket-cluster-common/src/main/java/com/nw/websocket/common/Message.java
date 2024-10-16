package com.nw.websocket.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 消息类，用于封装用户发送的消息
 * 该类实现了Serializable接口，以便于消息对象的序列化
 */
@Accessors(chain = true)
@Data
public class Message implements Serializable {
    // 用户ID，用于标识发送消息的用户，这是每个消息独有的关键信息
    private final String userId;
    // 消息内容，用于存储用户发送的具体消息内容
    private final String message;
    // 设备信息，可选，用于记录发送消息时的设备信息
    private String device = "";
}
