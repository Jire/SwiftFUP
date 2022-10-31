package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jire.swiftfup.client.FileChecksumsRequest;

/**
 * @author Jire
 */
public final class FileChecksumsRequestEncoder extends MessageToByteEncoder<FileChecksumsRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FileChecksumsRequest msg, ByteBuf out) {
        out.writeByte(0);
    }

}
