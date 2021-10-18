package org.jire.swiftfup.server.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import it.unimi.dsi.fastutil.ints.Int2BooleanMap
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap
import org.jire.swiftfup.server.FilePair.Companion.readFilePair
import org.jire.swiftfup.server.net.FileRequestResponses

/**
 * @author Jire
 */
class FileServerRequestDecoder(
	private val fileRequestResponses: FileRequestResponses
) : ByteToMessageDecoder() {
	
	private var crcs = false
	private val fulfilledRequests: Int2BooleanMap = Int2BooleanOpenHashMap()
	
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
				
				if (fulfilledRequests.get(filePair.bitpack))
					throw IllegalStateException("File pair was already fulfilled: $filePair")
				fulfilledRequests[filePair.bitpack] = true
				
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