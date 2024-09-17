package com.nw.im.common;

public interface MessageListener {
    void message(String userId, String message) throws Exception;
}
