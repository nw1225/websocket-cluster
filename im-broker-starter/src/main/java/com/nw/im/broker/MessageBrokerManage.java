package com.nw.im.broker;

public interface MessageBrokerManage {
    void send(String userId, String message);

    void send(String userId, String device, String message);
}
