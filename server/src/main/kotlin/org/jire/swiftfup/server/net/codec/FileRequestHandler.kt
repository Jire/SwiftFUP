package org.jire.swiftfup.server.net.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.net.FileResponses
import java.io.IOException

/**
 * @author Jire
 */
class FileRequestHandler(
    private val responses: FileResponses
) : SimpleChannelInboundHandler<FilePair>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FilePair) {
        val response = responses[msg]
            ?: throw IllegalStateException("Request was null for $msg")

        ctx.write(response.retainedDuplicate(), ctx.voidPromise())
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()

        val channel = ctx.channel()
        if (!channel.isWritable) {
            channel.config().isAutoRead = false
        }
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        if (channel.isWritable) {
            channel.config().isAutoRead = true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause !is IOException) {
            cause.printStackTrace()
        }

        ctx.close()
    }

}
