package com.nw.im.common;

import java.util.Set;

public interface MessageListener {
    void message(Set<String> userIds, String message) throws Exception;
}
