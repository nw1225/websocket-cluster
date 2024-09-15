package com.nw.im.common;

import java.util.UUID;

public interface Constants {
    String NODE_PORT = "websocket.node.port";
    String NODE_ID = "websocket.node.id";

    String nodeId = UUID.randomUUID().toString();

    String clientKeyPrefix = "websocket:client:";

}
