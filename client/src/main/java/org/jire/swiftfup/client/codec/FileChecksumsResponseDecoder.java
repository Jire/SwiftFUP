package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jire.swiftfup.client.FileChecksumsResponse;
import org.jire.swiftfup.client.FilePair;
import org.jire.swiftfup.client.GZIPDecompressor;

import java.util.List;

/**
 * @author Jire
 */
public final class FileChecksumsResponseDecoder extends ByteToMessageDecoder {

    private int size = -1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (size == -1) {
            if (!in.isReadable(4)) {
                return;
            }

            size = in.readInt();
        }

        if (!in.isReadable(size)) {
            return;
        }

        final byte[] compressedBytes = new byte[size];
        in.readBytes(compressedBytes);

        final byte[] decompressedBytes = GZIPDecompressor.getInstance().decompress(compressedBytes);
        final ByteBuf data = Unpooled.wrappedBuffer(decompressedBytes);

        final Int2IntMap fileToChecksum = new Int2IntOpenHashMap();

        final int indexCount = data.readUnsignedByte();
        for (int indexId = 0; indexId < indexCount; indexId++) {

            final int archiveCount = data.readUnsignedMedium();
            for (int archiveId = 0; archiveId < archiveCount; archiveId++) {

                final int crc32 = data.readInt();
                if (crc32 == 0) continue;

                final int filePair = FilePair.create(indexId, archiveId);

                fileToChecksum.put(filePair, crc32);
            }
        }

        data.release();

        final FileChecksumsResponse response = new FileChecksumsResponse(fileToChecksum);
        out.add(response);
    }

}
