package org.jire.swiftfp.server.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.jire.swiftfp.server.FilePair.Companion.readFilePair
import org.jire.swiftfp.server.net.FileRequestResponses

/**
 * @author Jire
 */
class FileServerRequestDecoder(
	private val fileRequestResponses: FileRequestResponses
) : ByteToMessageDecoder() {
	
	@Volatile
	var crcs = false
	
	override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
		if (!crcs) {
			val opcode = buf.readUnsignedByte().toInt()
			if (opcode != 0) throw IllegalArgumentException("Invalid CRC opcode: $opcode")
			
			ctx.writeAndFlush(fileRequestResponses.checksumsResponse.retainedDuplicate(), ctx.voidPromise())
			
			crcs = true
		}
		
		if (crcs) {
			var flush = false
			
			while (buf.isReadable(4)) {
				val opcode = buf.readUnsignedByte().toInt()
				if (opcode != 1) throw IllegalArgumentException("Invalid cache file opcode: $opcode")
				
				val filePair = buf.readFilePair()
				val response = fileRequestResponses[filePair] ?: continue
				ctx.write(response.retainedDuplicate(), ctx.voidPromise())
				
				flush = true
			}
			
			if (flush) ctx.flush()
		}
	}
	
	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		cause.printStackTrace()
		ctx.close()
	}
	
}