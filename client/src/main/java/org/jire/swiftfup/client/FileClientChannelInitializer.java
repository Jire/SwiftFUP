package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.jire.swiftfup.client.codec.HandshakeRequestEncoder;
import org.jire.swiftfup.client.codec.HandshakeResponseDecoder;
import org.jire.swiftfup.client.codec.HandshakeResponseHandler;

/**
 * @author Jire
 */
public final class FileClientChannelInitializer extends ChannelInitializer<Channel> {

    public static final String DECODER_HANDLER = "decoder";
    public static final String ENCODER_HANDLER = "encoder";
    public static final String TAIL_HANDLER = "tail";

    private final FileRequests fileRequests;

    public FileClientChannelInitializer(FileRequests fileRequests) {
        this.fileRequests = fileRequests;
    }

    @Override
    protected void initChannel(Channel ch) {
        final ChannelPipeline p = ch.pipeline();
        p.addLast(DECODER_HANDLER, new HandshakeResponseDecoder());
        p.addLast(ENCODER_HANDLER, new HandshakeRequestEncoder());
        p.addLast(TAIL_HANDLER, new HandshakeResponseHandler(fileRequests));
    }

}
