package org.jire.swiftfup.server.net

import com.displee.cache.CacheLibrary
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.directBuffer
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.FilePair.Companion.writeFilePair
import java.util.zip.CRC32

/**
 * @author Jire
 */
class FileRequestResponses {
	
	lateinit var checksumsResponse: ByteBuf
	
	private val bitpack2Response: Int2ObjectMap<ByteBuf> = Int2ObjectOpenHashMap()
	
	operator fun get(filePair: FilePair): ByteBuf? = bitpack2Response[filePair.bitpack]
	
	fun load(cachePath: String, print: Boolean = true) {
		val library = CacheLibrary.create(cachePath)
		val indices = library.indices()
		
		var checksumsComputed = 0
		val checksumsBuffer = directBuffer(indices.size * 7)
		
		if (print) println("[Building cache responses]")
		for (index in indices) {
			val archives = index.archives()
			if (print) print("    index ${index.id} [0]")
			
			var responsesBuilt = 0
			for (archive in archives) {
				val sector = index.readArchiveSector(archive.id)
				val data = sector?.data
				val found = data != null
				val dataSize = data?.size ?: 0
				if (dataSize < 1) continue
				
				val filePair = FilePair(index.id, archive.id)
				
				val byteBufSize = FilePair.SIZE_BYTES + 3 + dataSize
				val byteBuf = directBuffer(byteBufSize, byteBufSize)
					.writeFilePair(filePair)
					.writeMedium(dataSize)
				val crc = if (found) {
					byteBuf.writeBytes(data)
					
					CRC32().apply { update(data) }.value.toInt()
				} else 0
				checksumsBuffer
					.writeFilePair(filePair)
					.writeInt(crc)
				checksumsComputed++
				
				bitpack2Response[filePair.bitpack] = byteBuf.asReadOnly()
				if (print) {
					responsesBuilt++
					
					var backspaces = "\b"
					if (responsesBuilt == 1) backspaces += "\b"
					else repeat((responsesBuilt - 1).toString().length) {
						backspaces += '\b'
					}
					print("$backspaces${responsesBuilt}]")
				}
			}
			if (print) println()
		}
		if (print) println()
		
		checksumsResponse = directBuffer(checksumsBuffer.readableBytes() + 3).run {
			writeMedium(checksumsComputed)
			writeBytes(checksumsBuffer)
			checksumsBuffer.release()
			asReadOnly()
		}
	}
	
}