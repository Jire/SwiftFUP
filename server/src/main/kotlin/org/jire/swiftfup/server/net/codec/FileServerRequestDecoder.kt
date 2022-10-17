package org.jire.swiftfup.server.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.FilePair.Companion.readFilePair

/**
 * @author Jire
 */
class FileServerRequestDecoder : ByteToMessageDecoder() {

    private companion object {
        private const val UNSET_OPCODE = -1
    }

    private var opcode = UNSET_OPCODE

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (opcode == UNSET_OPCODE) {
            opcode = buf.readUnsignedByte().toInt()
        }

        out += when (opcode) {
            0 -> FilePair.checksumsFilePair

            1 -> {
                if (!buf.isReadable(3)) {
                    return
                }

                buf.readFilePair()
            }

            else -> throw DecoderException("Invalid opcode: $opcode")
        }

        opcode = UNSET_OPCODE
    }

}