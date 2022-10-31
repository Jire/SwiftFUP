package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.jire.swiftfup.client.codec.*;

/**
 * @author Jire
 */
public final class FileClientChannelInitializer extends ChannelInitializer<Channel> {

    private final FileRequests fileRequests;
    private final boolean reconnect;

    public FileClientChannelInitializer(FileRequests fileRequests, boolean reconnect) {
        this.fileRequests = fileRequests;
        this.reconnect = reconnect;
    }

    @Override
    protected void initChannel(Channel ch) {
        final ChannelHandler decoder =
                reconnect
                        ? new FileResponseDecoder()
                        : new FileChecksumsResponseDecoder();
        final ChannelHandler encoder =
                reconnect
                        ? new FileRequestEncoder()
                        : new FileChecksumsRequestEncoder();
        final ChannelHandler handler =
                reconnect
                        ? new FileResponseHandler(fileRequests)
                        : new FileChecksumsResponseHandler(fileRequests);

        final ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", decoder);
        p.addLast("encoder", encoder);
        p.addLast("handler", handler);
    }

}
