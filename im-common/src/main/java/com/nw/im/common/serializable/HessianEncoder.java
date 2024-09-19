package com.nw.im.common.serializable;

import com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

/**
 * Hessian编码器，用于将对象序列化为字节流
 * 继承自Netty的MessageToByteEncoder类，以实现对象到字节流的转换
 */
public class HessianEncoder extends MessageToByteEncoder<Object> {

    /**
     * 编码方法，将对象转换为字节流
     * 使用Hessian2Output进行序列化，将序列化后的字节流写入到ByteBuf中
     *
     * @param ctx 上下文对象，包含管道、通道和地址等信息
     * @param msg 待编码的消息对象
     * @param out 编码后的字节流将被写入的ByteBuf对象
     * @throws Exception 如果序列化或写入过程中发生错误，将抛出异常
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        Hessian2Output ho = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ho = new Hessian2Output(os);
            // 将msg对象写入到Hessian2Output中，实现序列化
            ho.writeObject(msg);
            // 刷新输出流，确保所有数据都已写入
            ho.flush();
            // 将序列化后的字节写入到ByteBuf中，以供传输
            byte[] bytes = os.toByteArray();
            out.writeBytes(bytes);
        } finally {
            // 关闭Hessian2Input和输入流，释放资源
            if (ho != null) {
                ho.close();
            }
        }
    }
}
