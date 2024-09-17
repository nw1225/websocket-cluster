package com.nw.im.broker;

public interface MessageBrokerManage {
    void send(String userId, String key, String message);
}
