package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jire.swiftfup.client.HandshakeRequest;

/**
 * @author Jire
 */
public final class HandshakeRequestEncoder extends MessageToByteEncoder<HandshakeRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, HandshakeRequest msg, ByteBuf out) {
        out.writeMedium(msg.getVersion());
    }

}
