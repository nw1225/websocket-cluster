package com.nw.im.connect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置TCP属性的类
 * 主要用于设置和管理WebSocket集群的TCP配置
 */
@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class TcpProperties {
    /**
     * 定义集群通信的端口号，默认为8188
     */
    private Integer port = 8188;
}
