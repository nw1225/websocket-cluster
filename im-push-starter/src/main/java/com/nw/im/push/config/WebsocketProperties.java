package com.nw.im.push.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class WebsocketProperties {
    private String topic;

}
