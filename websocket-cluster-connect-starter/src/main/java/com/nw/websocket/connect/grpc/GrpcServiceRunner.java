package com.nw.websocket.connect.grpc;

import com.nw.websocket.common.Message;
import com.nw.websocket.common.MessageListener;
import com.nw.websocket.common.MessageServiceGrpc;
import com.nw.websocket.common.MessageServiceProto;
import com.nw.websocket.connect.config.WebsocketClusterProperties;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.IOException;

/**
 * grpc服务启动器，实现ApplicationRunner接口，允许在Spring Boot应用启动时自动启动grpc服务
 */
@Slf4j
@RequiredArgsConstructor
public class GrpcServiceRunner implements ApplicationRunner {
    // 消息监听器，用于处理接收到的消息
    private final MessageListener messageListener;
    // grpc属性配置，包括端口号等
    private final WebsocketClusterProperties websocketClusterProperties;

    /**
     * 在应用启动完成后执行指定的操作
     * 此方法用于启动grpc服务
     *
     * @param args 应用程序启动参数
     */
    @Override
    public void run(ApplicationArguments args) throws IOException {
        // 获取配置的端口号
        Integer port = websocketClusterProperties.getPort();
        log.info("grpc服务端口:{}", port);
        Server server = ServerBuilder.forPort(port)
                .addService(new MessageServiceImpl(messageListener))
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }

    @RequiredArgsConstructor
    private static class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {
        private final MessageListener messageListener;

        @Override
        public StreamObserver<MessageServiceProto.Message> push(StreamObserver<MessageServiceProto.Message> responseObserver) {
            return new PushStreamObserver(messageListener,responseObserver);
        }
    }

    @RequiredArgsConstructor
    private static class PushStreamObserver implements StreamObserver<MessageServiceProto.Message> {
        private final MessageListener messageListener;
        private final StreamObserver<MessageServiceProto.Message> responseObserver;

        @Override
        public void onNext(MessageServiceProto.Message message) {
            messageListener.message(new Message(message.getUserId(), message.getMessage()).setDevice(message.getDevice()));
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("push error.", throwable);
        }

        @Override
        public void onCompleted() {
            responseObserver.onCompleted();
        }
    }
}
