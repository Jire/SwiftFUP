package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import org.jire.swiftfup.client.HandshakeResponse;

import java.util.List;

/**
 * @author Jire
 */
public final class HandshakeResponseDecoder extends ByteToMessageDecoder {

    public static final int NO_STATUS = -1,
            SUCCESS_STATUS = 0,
            VERSION_MISMATCH_STATUS = 1;

    private int status = NO_STATUS;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (status == NO_STATUS) {
            if (!in.isReadable(1)) {
                return;
            }

            status = in.readUnsignedByte();
        }

        if (status == SUCCESS_STATUS) {
            if (!in.isReadable(3)) {
                return;
            }

            final int version = in.readUnsignedMedium();
            out.add(new HandshakeResponse(status, version));
        } else if (status == VERSION_MISMATCH_STATUS) {
            throw new DecoderException("Version of server mismatched our version");
        } else {
            throw new DecoderException("Unknown status received: " + status);
        }
    }

}
