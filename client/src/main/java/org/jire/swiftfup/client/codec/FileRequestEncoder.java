package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jire.swiftfup.client.FileRequest;

/**
 * @author Jire
 */
public final class FileRequestEncoder extends MessageToByteEncoder<FileRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FileRequest msg, ByteBuf out) {
        out.writeByte(1);

        final int filePair = msg.getFilePair();
        out.writeMedium(filePair);
    }

}
