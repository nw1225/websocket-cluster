package com.nw.im.common.serializable;

import com.caucho.hessian.io.Hessian2Input;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.util.List;


public class HessianDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        ByteBuf in2 = in.retainedDuplicate();
        byte[] dst;
        if (in2.hasArray()) {//堆缓冲区模式
            dst = in2.array();
        } else {
            dst = new byte[in2.readableBytes()];
            in2.getBytes(in2.readerIndex(), dst);
        }
        in.skipBytes(in.readableBytes());

        ByteArrayInputStream is = new ByteArrayInputStream(dst);
        Hessian2Input hi = new Hessian2Input(is);
        Object obj = hi.readObject();
        try {
            out.add(obj);
        } finally {
            hi.close();
            is.close();
        }
    }
}
