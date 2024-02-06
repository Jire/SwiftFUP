package org.jire.swiftfup.server.net.codec

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.jire.swiftfup.server.net.FileResponses
import org.jire.swiftfup.server.net.FileServerChannelInitializer.Companion.DECODER_HANDLER
import org.jire.swiftfup.server.net.FileServerChannelInitializer.Companion.TAIL_HANDLER
import org.jire.swiftfup.server.net.HandshakeRequest
import org.jire.swiftfup.server.net.HandshakeResponse
import java.io.IOException

/**
 * @author Jire
 */
class HandshakeRequestHandler(
    private val version: Int,
    private val fileResponses: FileResponses
) : SimpleChannelInboundHandler<HandshakeRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HandshakeRequest) {
        val version = this.version
        if (version == msg.version) {
            ctx.writeAndFlush(HandshakeResponse.SUCCESS.buf.retainedDuplicate(), ctx.voidPromise())

            ctx.pipeline().replace(DECODER_HANDLER, DECODER_HANDLER, FileRequestDecoder())
            ctx.pipeline().replace(TAIL_HANDLER, TAIL_HANDLER, FileRequestHandler(fileResponses))
        } else {
            ctx.writeAndFlush(HandshakeResponse.VERSION_MISMATCH.buf.retainedDuplicate())
                .addListener(ChannelFutureListener.CLOSE)
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