
package com.nw.websocket.common;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * tcp断线处理
 */
public class ServiceHeartBeatHandler extends ChannelDuplexHandler {

    /**
     * 处理用户事件触发，主要用于心跳检测
     * 当检测到READER_IDLE状态时，关闭通道
     *
     * @param ctx 上下文对象，包含通道及其配置
     * @param evt 事件对象，可能为IdleStateEvent
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 判断事件是否为IdleStateEvent
        if (evt instanceof IdleStateEvent e) {
            // 判断IdleStateEvent的具体状态，如果是READER_IDLE，则关闭连接
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }
}
