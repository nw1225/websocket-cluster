package com.nw.im.connect.tcp;

import com.nw.im.common.MessageListener;
import com.nw.im.common.tcp.TcpNettyService;
import com.nw.im.connect.config.TcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class TcpNettyRunner implements ApplicationRunner {
    private final MessageListener messageListener;
    private final TcpProperties tcpProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer port = tcpProperties.getPort();
        TcpNettyService tcpNettyService = new TcpNettyService(port, messageListener);
        log.info("websocket cluster port:{}", port);
        Executors.newSingleThreadExecutor().execute(tcpNettyService::start);
    }
}
