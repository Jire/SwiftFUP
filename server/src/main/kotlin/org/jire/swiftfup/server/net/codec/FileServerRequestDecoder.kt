package org.jire.swiftfup.server.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.timeout.IdleStateEvent
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
	
	companion object {
		private val sizes = intArrayOf(0, 3)
	}
	
	private var fulfilledChecksums = false
	private val fulfilledRequests: Int2BooleanMap = Int2BooleanOpenHashMap()
	
	override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
		var flush = false
		
		while (buf.isReadable) {
			buf.markReaderIndex()
			
			/* read header */
			val opcode = buf.readUnsignedByte().toInt()
			val size = sizes[opcode]
			if (!buf.isReadable(size)) {
				buf.resetReaderIndex()
				break
			}
			
			/* handle opcode */
			when (opcode) {
				0 -> {
					if (fulfilledChecksums)
						throw IllegalStateException("Cannot request checksums multiple times!")
					fulfilledChecksums = true
					ctx.write(fileRequestResponses.checksumsResponse.retainedDuplicate(), ctx.voidPromise())
					flush = true
				}
				1 -> {
					val filePair = buf.readFilePair()
					
					if (fulfilledRequests.get(filePair.bitpack))
						throw IllegalStateException("File pair was already fulfilled: $filePair")
					fulfilledRequests[filePair.bitpack] = true
					
					val response = fileRequestResponses[filePair] ?: continue
					
					println("REQUEST ${filePair.index}:${filePair.file}")
					ctx.write(response.retainedDuplicate(), ctx.voidPromise())
					
					flush = true
				}
			}
		}
		
		if (flush) ctx.flush()
	}
	
	override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
		if (evt is IdleStateEvent) {
			IllegalStateException("Channel ${ctx.channel().remoteAddress()} was idle!").printStackTrace()
			ctx.close()
		}
	}
	
	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		cause.printStackTrace()
		ctx.close()
	}
	
}