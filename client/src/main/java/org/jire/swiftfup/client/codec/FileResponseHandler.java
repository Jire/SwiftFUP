package org.jire.swiftfup.client.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jire.swiftfup.client.FileRequests;
import org.jire.swiftfup.client.FileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jire
 */
public final class FileResponseHandler extends SimpleChannelInboundHandler<FileResponse> {

    private static final Logger logger = LoggerFactory.getLogger(FileResponseHandler.class);

    private final FileRequests fileRequests;

    public FileResponseHandler(FileRequests fileRequests) {
        this.fileRequests = fileRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileResponse msg) {
        fileRequests.notify(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);
        
        ctx.close();
    }

}
