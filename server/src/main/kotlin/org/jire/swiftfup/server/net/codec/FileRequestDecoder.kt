package org.jire.swiftfup.server.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.jire.swiftfup.server.FilePair.Companion.readFilePair

/**
 * @author Jire
 */
class FileRequestDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable(3)) {
            return
        }

        out += buf.readFilePair()
    }

}