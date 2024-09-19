package com.nw.im.common.serializable;

import com.caucho.hessian.io.Hessian2Input;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * HessianDecoder类是基于Netty的MessageToMessageDecoder的泛型类，用于解码ByteBuf类型的消息。
 * 它实现了将接收到的ByteBuf数据反序列化为Hessian对象的功能。
 */
public class HessianDecoder extends MessageToMessageDecoder<ByteBuf> {

    /**
     * decode方法是解码操作的核心实现。
     * 它从ByteBuf中读取数据，将其转换为Hessian对象，并添加到out列表中。
     *
     * @param ctx 上下文对象，包含管道、通道和地址等信息。
     * @param in  输入的ByteBuf数据，包含待解码的数据。
     * @param out 解码后的内容列表，通常包含一个或多个对象。
     * @throws Exception 解码过程中可能抛出的异常。
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 创建输入数据的复用副本，以支持多次读取操作
        ByteBuf in2 = in.retainedDuplicate();
        byte[] dst;

        // 判断是否为堆缓冲区模式，如果是，则直接使用数组，以减少复制开销
        if (in2.hasArray()) {
            dst = in2.array();
        } else {
            // 非堆缓冲区模式，读取所有可读字节到新分配的字节数组
            dst = new byte[in2.readableBytes()];
            in2.getBytes(in2.readerIndex(), dst);
        }

        // 跳过所有剩余字节，这里可以进行一些额外的处理或验证
        in.skipBytes(in.readableBytes());

        // 将字节数组转换为输入流
        Hessian2Input hi = null;
        try (ByteArrayInputStream is = new ByteArrayInputStream(dst)) {
            hi = new Hessian2Input(is);

            // 读取并反序列化Hessian对象
            Object obj = hi.readObject();
            // 将反序列化的对象添加到输出列表
            out.add(obj);
        } finally {
            // 关闭Hessian2Input和输入流，释放资源
            if (hi != null) {
                hi.close();
            }
            // 释放ByteBuf资源
            in2.release();
        }
    }
}
