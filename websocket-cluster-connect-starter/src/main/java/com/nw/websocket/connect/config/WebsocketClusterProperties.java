package com.nw.websocket.connect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置grpc属性的类
 * 主要用于设置和管理WebSocket集群的grpc配置
 */
@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class WebsocketClusterProperties {
    /**
     * 定义集群通信的端口号，默认为8188
     */
    private Integer port = 8188;

    private String pingText = "ping";

    private String pongText = "pong";
}
