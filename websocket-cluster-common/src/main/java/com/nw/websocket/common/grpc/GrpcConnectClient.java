package com.nw.websocket.common.grpc;

import com.nw.websocket.common.ChannelManager;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * grpc客户端实现
 */
@RequiredArgsConstructor
public class GrpcConnectClient {
    private final ChannelManager channelManager;


    public void connect(String host, Integer port, String nodeId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        channelManager.addChannel(channel, nodeId);
    }

    public void closeConnect(String serviceId) {
        ManagedChannel channel = channelManager.getChannel(serviceId);
        if (Objects.nonNull(channel)) {
            channelManager.removeChannel(channel);
            channel.shutdown();
        }
    }
}
