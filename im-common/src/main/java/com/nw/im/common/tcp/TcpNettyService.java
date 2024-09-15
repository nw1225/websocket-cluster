package com.nw.im.common.tcp;


import com.nw.im.common.MessageListener;
import com.nw.im.common.ServiceHeartBeatHandler;
import com.nw.im.common.serializable.HessianDecoder;
import com.nw.im.common.serializable.HessianEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TcpNettyService {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final Integer nodePort;
    private final MessageListener messageListener;

    public final void start() {
        try {
            //创建服务端启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)//Nio模式
                    .option(ChannelOption.SO_BACKLOG, 1024)//标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。主要是作用于boss线程，用于处理新连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//启用心跳保活机制,主要作用与worker线程，也就是已创建的channel。
                    .handler(new LoggingHandler("DEBUG"))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new HessianEncoder())
                                    .addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4))
                                    .addLast(new HessianDecoder())
                                    .addLast(new ServiceHeartBeatHandler())
                                    .addLast(new TcpNettyHandler(messageListener));
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(nodePort).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
