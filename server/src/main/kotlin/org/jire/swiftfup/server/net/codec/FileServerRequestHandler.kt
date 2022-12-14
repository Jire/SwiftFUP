package org.jire.swiftfup.server.net.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.net.FileRequestResponses
import java.io.IOException

/**
 * @author Jire
 */
class FileServerRequestHandler(
    private val responses: FileRequestResponses
) : SimpleChannelInboundHandler<FilePair>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FilePair) {
        if (msg.bitpack == FilePair.checksumsFilePair.bitpack) {
            ctx.write(responses.checksumsResponse.retainedDuplicate(), ctx.voidPromise())
        } else {
            val response = responses[msg]
                ?: throw IllegalStateException("Request was null for $msg")

            ctx.write(response.retainedDuplicate(), ctx.voidPromise())
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause !is IOException) {
            cause.printStackTrace()
        }
        ctx.close()
    }

}
