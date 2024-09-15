package com.nw.im.connect;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface WebsocketSessionManage {
    void put(WebSocketSession session, String userId, String device);

    void delete(String userId, String device);

    void sendMessage(String userId, String device, String message);

    void sendMessage(String userId, String message);
}
