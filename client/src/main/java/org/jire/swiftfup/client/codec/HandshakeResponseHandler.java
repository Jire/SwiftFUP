package org.jire.swiftfup.client.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.jire.swiftfup.client.FileRequests;
import org.jire.swiftfup.client.HandshakeRequest;
import org.jire.swiftfup.client.HandshakeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jire.swiftfup.client.FileClientChannelInitializer.*;

/**
 * @author Jire
 */
public final class HandshakeResponseHandler extends SimpleChannelInboundHandler<HandshakeResponse> {

    public static final AttributeKey<HandshakeResponse> HANDSHAKE_RESPONSE_ATTRIBUTE_KEY
            = AttributeKey.newInstance("HANDSHAKE_RESPONSE");

    private static final Logger logger = LoggerFactory.getLogger(HandshakeResponseHandler.class);

    private final FileRequests fileRequests;

    public HandshakeResponseHandler(FileRequests fileRequests) {
        this.fileRequests = fileRequests;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new HandshakeRequest(), ctx.voidPromise());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeResponse msg) {
        final ChannelPipeline p = ctx.pipeline();
        p.replace(DECODER_HANDLER, DECODER_HANDLER, new FileResponseDecoder());
        p.replace(ENCODER_HANDLER, ENCODER_HANDLER, new FileRequestEncoder());
        p.replace(TAIL_HANDLER, TAIL_HANDLER, new FileResponseHandler(fileRequests));

        ctx.channel().attr(HANDSHAKE_RESPONSE_ATTRIBUTE_KEY).set(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);

        ctx.close();
    }

}
