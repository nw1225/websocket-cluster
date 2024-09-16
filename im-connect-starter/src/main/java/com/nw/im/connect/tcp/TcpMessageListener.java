package com.nw.im.connect.tcp;

import com.nw.im.common.MessageListener;
import com.nw.im.connect.WebsocketSessionManage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class TcpMessageListener implements MessageListener {
    private final WebsocketSessionManage websocketSessionManage;

    @Override
    public void message(String userId, String message) throws Exception {
        log.debug("userId:{} message:{}", userId, message);
        if (!StringUtils.hasLength(userId)) {
            return;
        }
        websocketSessionManage.sendMessage(userId, message);
    }
}
