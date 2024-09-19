package com.nw.websocket.push.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置WebSocket相关属性的类
 * 主要用于接收和处理WebSocket集群配置
 */
@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class WebsocketProperties {
    /**
     * WebSocket集群的消息主题
     * 默认值为"im-push"
     */
    private String topic="im-push";

}
