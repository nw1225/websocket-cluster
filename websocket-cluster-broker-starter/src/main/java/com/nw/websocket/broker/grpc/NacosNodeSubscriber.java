package com.nw.websocket.broker.grpc;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.nw.websocket.broker.config.WebsocketProperties;
import com.nw.websocket.common.Constants;
import com.nw.websocket.common.grpc.GrpcConnectClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Nacos节点订阅者
 * 用于监听Nacos服务实例的上下线事件，并相应地建立或关闭与这些实例的grpc连接
 */
@Slf4j
@RequiredArgsConstructor
public class NacosNodeSubscriber implements EventListener {
    // Nacos服务管理器，用于与Nacos服务进行交互
    private final NacosServiceManager nacosServiceManager;
    // grpc客户端，用于建立和管理grpc连接
    private final GrpcConnectClient grpcConnectClient;
    // Websocket属性，包含服务名等配置
    private final WebsocketProperties websocketProperties;
    // 实例缓存，键为节点ID，值为实例详情
    private final Map<String, Instance> instances = new ConcurrentHashMap<>();

    /**
     * 处理Nacos服务实例的事件
     * 主要功能是过滤出有用的实例，并分别调用上线和下线处理方法
     *
     * @param event Nacos事件，包含服务实例的上线或下线信息
     */
    @Override
    public void onEvent(Event event) {
        List<Instance> instances = ((NamingEvent) event).getInstances();
        List<Instance> list = instances
                .stream()
                .filter(i -> {
                    Map<String, String> metadata = i.getMetadata();
                    String nodeId = metadata.get(Constants.NODE_ID);
                    String nodePort = metadata.get(Constants.NODE_PORT);
                    return StringUtils.hasLength(nodeId) && StringUtils.hasLength(nodePort);
                }).toList();

        online(list);
        offline(list);
    }

    /**
     * 处理实例下线逻辑
     * 通过比较当前实例列表和缓存中的实例列表，找出已下线的节点，并关闭与这些节点的连接
     *
     * @param list 当前活跃的实例列表
     */
    private void offline(List<Instance> list) {
        Set<String> nodeIds = list.stream().map(i -> i.getMetadata().get(Constants.NODE_ID)).collect(Collectors.toSet());
        Set<String> offlineNodeIds = instances.keySet().stream().filter(k -> !nodeIds.contains(k)).collect(Collectors.toSet());
        offlineNodeIds.forEach(nodeId -> {
            grpcConnectClient.closeConnect(nodeId);
            instances.remove(nodeId);
            log.debug("{}下线", nodeId);
        });
    }

    /**
     * 处理实例上线逻辑
     * 对于新出现的实例，尝试与其建立grpc连接，并将其信息加入缓存
     *
     * @param list 新发现的实例列表
     */
    private void online(List<Instance> list) {
        list.forEach(i -> {
            Map<String, String> metadata = i.getMetadata();
            String nodeId = metadata.get(Constants.NODE_ID);
            instances.computeIfAbsent(nodeId, (key) -> {
                grpcConnectClient.connect(i.getIp(), Integer.parseInt(metadata.get(Constants.NODE_PORT)), nodeId);
                log.debug("{}上线", nodeId);
                return i;
            });
        });
    }

    /**
     * 注册订阅者
     * 在Nacos服务中订阅指定的服务名，以便接收实例的变更事件
     *
     * @throws NacosException 如果与Nacos服务通信失败
     */
    @PostConstruct
    public void registerSubscriber() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService();
        namingService.subscribe(websocketProperties.getConnectServiceName(), this);
    }
}
