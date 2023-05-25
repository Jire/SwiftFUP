package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jire.swiftfup.client.FileChecksumsResponse;

import java.util.List;

/**
 * @author Jire
 */
public final class FileChecksumsResponseDecoder extends ByteToMessageDecoder {

    private int size = -1;
    private Int2IntMap fileToChecksum;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (size == -1 || fileToChecksum == null) {
            if (!in.isReadable(3)) {
                return;
            }

            size = in.readUnsignedMedium();
            fileToChecksum = new Int2IntOpenHashMap(size);
        }

        if (!in.isReadable(7)) {
            return;
        }

        final int filePair = in.readUnsignedMedium();
        final int crc32 = in.readInt();

        fileToChecksum.put(filePair, crc32);

        if (fileToChecksum.size() >= size) {
            final FileChecksumsResponse response = new FileChecksumsResponse(fileToChecksum);
            out.add(response);
        }
    }

}
