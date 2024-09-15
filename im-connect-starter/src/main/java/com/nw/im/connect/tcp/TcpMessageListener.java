package com.nw.im.connect.tcp;

import com.nw.im.connect.WebsocketSessionManage;
import com.nw.im.common.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class TcpMessageListener implements MessageListener {
    private final WebsocketSessionManage websocketSessionManage;

    @Override
    public void message(Set<String> userIds, String message) throws Exception {
        log.debug("userIds:{} message:{}", userIds, message);
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        userIds.forEach(userId -> {
            websocketSessionManage.sendMessage(userId, message);
        });
    }
}
