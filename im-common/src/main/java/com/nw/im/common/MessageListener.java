package com.nw.im.common;

import java.util.Set;

public interface MessageListener {
    void message(String userId, String message) throws Exception;
}
