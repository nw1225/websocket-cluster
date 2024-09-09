package com.nw.im.connect;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "websocket")
public class WebsocketProperties {
    private String path = "/ws";
}
