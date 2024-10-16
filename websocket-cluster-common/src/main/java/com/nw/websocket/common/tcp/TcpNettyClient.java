package com.nw.websocket.common.tcp;

import com.nw.websocket.common.ChannelManager;
import com.nw.websocket.common.serializable.HessianDecoder;
import com.nw.websocket.common.serializable.HessianEncoder;
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

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP客户端实现，使用Netty进行网络通信
 * 是否考虑每个函数和变量上加上完整注释,方便阅读?
 */
public class TcpNettyClient {
    private final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final ChannelManager channelManager;
    private final Bootstrap bootstrap = new Bootstrap();

    public TcpNettyClient(ChannelManager channelManager) {
        this.channelManager = channelManager;
        init();
    }


    private void init() {
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 50, 0, TimeUnit.SECONDS))
                                .addLast(new LengthFieldPrepender(4)).addLast(new HessianEncoder())
                                .addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4))
                                .addLast(new HessianDecoder()).addLast(new HeartBeatHandler())
                                .addLast(new SimpleChannelInboundHandler<>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) {
                                    }
                                });
                    }
                });
    }

    public void connect(String host, Integer port, String nodeId) {
        this.connect(InetSocketAddress.createUnresolved(host, port), nodeId, new AtomicInteger(0), 2);
    }

    private void connect(InetSocketAddress address, String nodeId, AtomicInteger retryCount, long delay) {
        bootstrap.connect(address).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channelManager.addChannel(future.channel(), nodeId);
            } else {
                future.channel().eventLoop().schedule(() -> {
                    int count = retryCount.incrementAndGet();
                    if (count >= 3) {
                        return;
                    }
                    connect(address, nodeId, retryCount, delay * 2);
                }, delay, TimeUnit.SECONDS);
            }
        }).awaitUninterruptibly(5, TimeUnit.SECONDS); // 增加超时时间
    }

    public void closeConnect(String serviceId) {
        Channel channel = channelManager.getChannel(serviceId);
        if (Objects.nonNull(channel)) {
            channelManager.removeChannel(channel);
            channel.close().syncUninterruptibly();
        }
    }

    public void destroy() {
        eventLoopGroup.shutdownGracefully();
    }

    private static class HeartBeatHandler extends ChannelDuplexHandler {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent e) {
                if (e.state() == IdleState.WRITER_IDLE) {
                    ctx.writeAndFlush("ping");
                }
            }
        }
    }
}
