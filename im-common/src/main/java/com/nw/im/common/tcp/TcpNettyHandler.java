package com.nw.im.common.tcp;


import com.nw.im.common.MessageListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class TcpNettyHandler extends SimpleChannelInboundHandler<Message> {
    private final MessageListener messageListener;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        messageListener.message(msg.getUserId(), msg.getMessage());
    }
}
