package org.jire.swiftfup.client.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jire.swiftfup.client.FileChecksumsResponse;
import org.jire.swiftfup.client.FileRequests;

/**
 * @author Jire
 */
public final class FileChecksumsResponseHandler extends SimpleChannelInboundHandler<FileChecksumsResponse> {

    private final FileRequests fileRequests;

    public FileChecksumsResponseHandler(FileRequests fileRequests) {
        this.fileRequests = fileRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileChecksumsResponse msg) {
        final ChannelPipeline p = ctx.pipeline();
        p.replace("decoder", "decoder", new FileResponseDecoder());
        p.replace("encoder", "encoder", new FileRequestEncoder());
        p.replace(this, "handler", new FileResponseHandler(fileRequests));

        fileRequests.notifyChecksums(msg);
    }

}
