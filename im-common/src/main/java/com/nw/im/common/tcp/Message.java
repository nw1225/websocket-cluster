package com.nw.im.common.tcp;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Builder
@Data
public class Message implements Serializable {
    private String message;
    private Set<String> userIds;
}

