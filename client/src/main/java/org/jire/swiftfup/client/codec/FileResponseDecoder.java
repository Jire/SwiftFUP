package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jire.swiftfup.client.FileResponse;

import java.util.List;

/**
 * @author Jire
 */
public final class FileResponseDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!in.isReadable(6)) {
            return;
        }
        in.markReaderIndex();

        final int filePair = in.readUnsignedMedium();

        final int dataSize = in.readUnsignedMedium();
        if (!in.isReadable(dataSize)) {
            in.resetReaderIndex();
            return;
        }

        final byte[] data = dataSize > 0 ? new byte[dataSize] : null;
        if (data != null) in.readBytes(data);

        final FileResponse fileResponse = new FileResponse(filePair, data);
        out.add(fileResponse);
    }

}
