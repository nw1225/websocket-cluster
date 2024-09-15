package com.nw.im.router.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class WebsocketProperties {
    private String connectServiceName;

}
