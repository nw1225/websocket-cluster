package com.nw.im.connect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class TcpProperties {
    private Integer port = 8188;

}
