package org.jire.swiftfup.server

import io.netty.buffer.ByteBuf

/**
 * @author Jire
 */
@JvmInline
value class FilePair(val bitpack: Int) {
	
	constructor(index: Int, file: Int) : this(bitpack(index, file))
	
	val index get() = index(bitpack)
	val file get() = file(bitpack)
	
	operator fun component1() = index
	operator fun component2() = file
	
	companion object {
		
		const val SIZE_BYTES = 3
		const val SIZE_BITS = Byte.SIZE_BITS * SIZE_BYTES
		
		fun bitpack(index: Int, file: Int): Int {
			require(index and 0x1F.inv() == 0) { "invalid index $index:$file" }
			require(file and 0x7FFFF.inv() == 0) { "invalid file $index:$file" }
			return (index and 0x1F) or ((file and 0x7FFFF) shl 5)
		}
		
		fun index(bitpack: Int) = bitpack and 0x1F
		fun file(bitpack: Int) = (bitpack ushr 5) and 0x7FFFF
		
		fun ByteBuf.readFilePair() = FilePair(readMedium())
		fun ByteBuf.writeFilePair(filePair: FilePair) = writeMedium(filePair.bitpack)
		
	}
	
}