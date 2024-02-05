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

    private int filePair = -1;
    private int dataSize = -1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (filePair == -1) {
            if (!in.isReadable(7)) {
                return;
            }

            filePair = in.readUnsignedMedium();
            dataSize = in.readInt();
        }

        if (!in.isReadable(dataSize)) {
            return;
        }

        try {
            final byte[] data = dataSize > 0 ? new byte[dataSize] : null;
            if (data != null) in.readBytes(data);

            final FileResponse fileResponse = new FileResponse(filePair, data);
            out.add(fileResponse);
        } finally {
            dataSize = -1;
            filePair = -1;
        }
    }

}
