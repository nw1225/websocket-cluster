package com.nw.im.common.tcp;


import com.nw.im.common.ChannelManager;
import com.nw.im.common.serializable.HessianDecoder;
import com.nw.im.common.serializable.HessianEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class TcpNettyClient {
    private final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final ChannelManager channelManager;

    private final Bootstrap bootstrap = new Bootstrap();


    public TcpNettyClient(ChannelManager channelManager) {
        this.channelManager = channelManager;
        init();
    }

    private void init() {
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new IdleStateHandler(0, 50, 0, TimeUnit.SECONDS)).addLast(new LengthFieldPrepender(4)).addLast(new HessianEncoder()).addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4)).addLast(new HessianDecoder()).addLast(new HeartBeatHandler()).addLast(new SimpleChannelInboundHandler() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

                    }
                });
            }
        });
    }

    public void connect(String host, Integer port, String nodeId) {
        bootstrap.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channelManager.addChannel(future.channel(), nodeId);
            } else {
                future.channel().eventLoop().schedule(() -> connect(host, port, nodeId), 2, TimeUnit.SECONDS);
            }
        });
    }
    public void closeConnect(String serviceId) {
        Channel channel = channelManager.getChannel(serviceId);
        if (Objects.nonNull(channel)) {
            channelManager.removeChannel(channel);
            channel.close();
        }
    }

    public void destroy() {
        eventLoopGroup.shutdownGracefully();
    }
    private static class HeartBeatHandler extends ChannelDuplexHandler {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent e) {
                if (e.state() == IdleState.WRITER_IDLE) {
                    ctx.writeAndFlush("ping");
                }
            }
        }
    }
}
