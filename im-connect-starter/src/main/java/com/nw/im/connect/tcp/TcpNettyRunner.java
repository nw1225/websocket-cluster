package com.nw.im.connect.tcp;

import com.nw.im.common.MessageListener;
import com.nw.im.common.tcp.TcpNettyService;
import com.nw.im.connect.config.TcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.concurrent.Executors;

/**
 * TCP Netty服务启动器，实现ApplicationRunner接口，允许在Spring Boot应用启动时自动启动TCP服务
 */
@Slf4j
@RequiredArgsConstructor
public class TcpNettyRunner implements ApplicationRunner {
    // 消息监听器，用于处理接收到的消息
    private final MessageListener messageListener;
    // TCP属性配置，包括端口号等
    private final TcpProperties tcpProperties;

    /**
     * 在应用启动完成后执行指定的操作
     * 此方法用于启动TCP Netty服务
     *
     * @param args 应用程序启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 获取配置的端口号
        Integer port = tcpProperties.getPort();
        // 创建TCP Netty服务实例，传入端口号和消息监听器
        TcpNettyService tcpNettyService = new TcpNettyService(port, messageListener);
        // 记录TCP服务启动的日志信息
        log.info("TCP服务端口:{}", port);
        // 使用单线程执行器启动TCP Netty服务
        Executors.newSingleThreadExecutor().execute(tcpNettyService::start);
    }
}
