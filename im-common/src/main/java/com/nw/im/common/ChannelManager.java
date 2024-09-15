package com.nw.im.common;

import io.netty.channel.Channel;


public interface ChannelManager {


    void addChannel(Channel channel, String nodeId);

    void removeChannel(Channel channel);

    String getClient(Channel channel);

    Channel getChannel(String client);

    Boolean online(String client);
}
