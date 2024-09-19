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

/**
 * TcpNettyService类用于启动和管理一个TCP服务端，基于Netty框架实现
 */
@RequiredArgsConstructor
public class TcpNettyService {
    // Boss线程池，用于处理连接和断开连接的操作
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    // Worker线程池，用于处理网络IO操作
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    // 服务端监听的端口号
    private final Integer nodePort;
    // 消息监听器，用于处理接收到的消息
    private final MessageListener messageListener;

    /**
     * 启动TCP服务端
     * 使用Netty的ServerBootstrap助手配置和启动服务器
     * 配置了线程池、通道类型、各种选项以及处理器
     */
    public final void start() {
        try {
            // 创建服务端启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)// 设置Nio模式
                    .option(ChannelOption.SO_BACKLOG, 1024)// 设置标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                    .childOption(ChannelOption.SO_KEEPALIVE, true)// 启用心跳保活机制
                    .handler(new LoggingHandler("DEBUG"))// 添加日志处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))// 添加读写空闲状态处理器
                                    .addLast(new LengthFieldPrepender(4))// 添加长度字段前置处理器
                                    .addLast(new HessianEncoder())// 添加Hessian编码器
                                    .addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4))// 添加基于长度字段的帧解码器
                                    .addLast(new HessianDecoder())// 添加Hessian解码器
                                    .addLast(new ServiceHeartBeatHandler())// 添加服务心跳处理器
                                    .addLast(new TcpNettyHandler(messageListener));// 添加自定义的通道处理器
                        }
                    });
            // 绑定端口并同步等待成功
            ChannelFuture channelFuture = serverBootstrap.bind(nodePort).sync();
            // 等待通道关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException("启动服务端失败", e);
        } finally {
            // 关闭线程池，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
