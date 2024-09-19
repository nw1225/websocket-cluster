package com.nw.im.common;

import java.util.UUID;

/**
 * 常量接口，用于定义和管理WebSocket通信中使用的常量
 */
public interface Constants {
    // WebSocket节点端口的配置键
    String NODE_PORT = "websocket.node.port";
    // WebSocket节点ID的配置键
    String NODE_ID = "websocket.node.id";

    // 使用UUID作为默认的节点ID，确保节点ID的唯一性
    String nodeId = UUID.randomUUID().toString();

    // 客户端键的前缀模式，用于在存储或检索时格式化键
    // {%s} 占位符用于插入节点ID，:%s 用于插入用户ID
    String clientKeyPrefix = "websocket:client:{%s}:%s";

    // 用户ID的键，用于标识客户端连接所属的用户
    String userId = "userId";

    // 设备信息的键，用于存储或查询客户端连接时的设备信息
    String device = "device";
}
