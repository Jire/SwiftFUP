package org.jire.swiftfup.client.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jire.swiftfup.client.FileRequests;
import org.jire.swiftfup.client.FileResponse;

/**
 * @author Jire
 */
public final class FileResponseHandler extends SimpleChannelInboundHandler<FileResponse> {

    private final FileRequests fileRequests;

    public FileResponseHandler(FileRequests fileRequests) {
        this.fileRequests = fileRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileResponse msg) {
        fileRequests.notify(msg);
    }

}
