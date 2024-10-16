package com.nw.websocket.connect.grpc;

import com.nw.websocket.common.Message;
import com.nw.websocket.common.MessageListener;
import com.nw.websocket.connect.WebsocketSessionManage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * grpc消息监听器类，实现了MessageListener接口
 * 该类用于处理grpc渠道接收到的消息，并通过Websocket进行转发
 */
@Slf4j
@RequiredArgsConstructor
public class GrpcMessageListener implements MessageListener {
    // Websocket会话管理器，用于发送消息到Websocket客户端
    private final WebsocketSessionManage websocketSessionManage;

    /**
     * 接收并处理消息
     * 根据消息中的用户ID和设备信息，将消息转发到对应的Websocket客户端
     * 如果消息中的用户ID无效（即用户ID为空或长度为0），则不进行任何操作
     * 如果消息中包含了设备信息，则根据用户ID和设备信息发送消息
     * 否则，根据用户ID将消息发送到所有设备
     *
     * @param msg 接收到的消息对象
     */
    @Override
    public void message(Message msg) {
        // 记录接收到的消息的日志信息
        log.debug("userId:{} message:{}", msg.getUserId(), msg.getMessage());

        // 如果用户ID无效，则直接返回，不处理后续逻辑
        if (!StringUtils.hasLength(msg.getUserId())) {
            return;
        }
        String device = msg.getDevice();
        // 如果设备信息非空且有长度，则根据用户ID、设备信息发送消息
        if (StringUtils.hasLength(device)) {
            websocketSessionManage.sendMessage(msg.getUserId(), msg.getDevice(), msg.getMessage());
        } else {
            // 如果设备信息为空，根据用户ID发送消息到所有设备
            websocketSessionManage.sendMessage(msg.getUserId(), msg.getMessage());
        }
    }
}
