package com.nw.websocket.common.tcp;

import com.nw.websocket.common.Message;
import com.nw.websocket.common.MessageListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

/**
 * TCP协议的Netty处理器实现类
 * 该类负责处理接收到的Message对象，并将消息传递给MessageListener
 */
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class TcpNettyHandler extends SimpleChannelInboundHandler<Message> {
    // 消息监听器，用于处理接收到的消息
    private final MessageListener messageListener;

    /**
     * 当通道读取到消息时触发该方法
     * 该方法将接收到的消息传递给消息监听器进行处理
     *
     * @param ctx 上下文对象，包含通道和工厂信息
     * @param msg 接收到的消息对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 将接收到的消息传递给消息监听器进行处理
        messageListener.message(msg);
    }
}
