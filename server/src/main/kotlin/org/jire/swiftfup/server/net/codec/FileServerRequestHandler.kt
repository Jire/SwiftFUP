package org.jire.swiftfup.server.net.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import it.unimi.dsi.fastutil.ints.Int2BooleanMap
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.net.FileRequestResponses
import java.io.IOException

/**
 * @author Jire
 */
class FileServerRequestHandler(
    private val responses: FileRequestResponses
) : SimpleChannelInboundHandler<FilePair>() {

    private var fulfilledChecksums = false
    private val fulfilledPairs: Int2BooleanMap = Int2BooleanOpenHashMap()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FilePair) {
        if (msg.bitpack == FilePair.checksumsFilePair.bitpack) {
            if (fulfilledChecksums) {
                throw IllegalStateException("Cannot request checksums multiple times!")
            }
            fulfilledChecksums = true

            ctx.write(responses.checksumsResponse.retainedDuplicate(), ctx.voidPromise())
        } else {
            if (fulfilledPairs.get(msg.bitpack)) {
                throw IllegalStateException("File pair was already fulfilled: $msg")
            }
            fulfilledPairs[msg.bitpack] = true

            val response = responses[msg]
                ?: throw IllegalStateException("Request was null for $msg")

            ctx.write(response.retainedDuplicate(), ctx.voidPromise())
        }
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
            ctx.read()
        } else {
            ctx.flush()
            if (!channel.isWritable) {
                channel.config().isAutoRead = false
            }
        }
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            ctx.close()
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
