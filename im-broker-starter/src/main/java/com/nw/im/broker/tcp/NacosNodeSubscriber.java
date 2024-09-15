package com.nw.im.broker.tcp;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.nw.im.common.Constants;
import com.nw.im.common.tcp.TcpNettyClient;
import com.nw.im.broker.config.WebsocketProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class NacosNodeSubscriber implements EventListener {
    private final NacosServiceManager nacosServiceManager;
    private final TcpNettyClient tcpNettyClient;
    private final WebsocketProperties websocketProperties;
    private final Map<String, Instance> instances = new ConcurrentHashMap<>();

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

    private void offline(List<Instance> list) {
        Set<String> nodeIds = list.stream().map(i -> i.getMetadata().get(Constants.NODE_ID)).collect(Collectors.toSet());
        Set<String> offlineNodeIds = instances.keySet().stream().filter(k -> !nodeIds.contains(k)).collect(Collectors.toSet());
        offlineNodeIds.forEach(nodeId -> {
            tcpNettyClient.closeConnect(nodeId);
            instances.remove(nodeId);
            log.debug("{}下线", nodeId);
        });
    }

    private void online(List<Instance> list) {
        list.forEach(i -> {
            Map<String, String> metadata = i.getMetadata();
            String nodeId = metadata.get(Constants.NODE_ID);
            instances.computeIfAbsent(nodeId, (key) -> {
                tcpNettyClient.connect(i.getIp(), Integer.parseInt(metadata.get(Constants.NODE_PORT)), nodeId);
                log.debug("{}上线", nodeId);
                return i;
            });
        });
    }

    @PostConstruct
    public void registerSubscriber() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService();
        namingService.subscribe(websocketProperties.getConnectServiceName(), this);
    }

    @PreDestroy
    public void destroy() {
        tcpNettyClient.destroy();
    }
}
