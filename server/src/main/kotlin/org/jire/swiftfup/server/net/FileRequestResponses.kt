package org.jire.swiftfup.server.net

import com.displee.cache.CacheLibrary
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.directBuffer
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.jire.swiftfup.common.GzipCompression
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

        val checksumsBuffer = directBuffer(indices.size * 8)
        checksumsBuffer.writeByte(indices.size)

        if (print) println("[Building cache responses]")
        for (index in indices) {
            val indexId = index.id

            val archives = index.archives()
            if (print) print("    index $indexId [0]")

            checksumsBuffer
                .writeMedium(archives.size)

            var i = 0
            var responsesBuilt = 0
            for (archive in archives) {
                val archiveId = archive.id
                val ii = i++
                if (archiveId != ii)
                    throw IllegalStateException("failed index $indexId archive $archiveId because ii was $ii")

                val sector = index.readArchiveSector(archiveId)
                val data = sector?.data
                val dataSize = data?.size ?: 0
                if (dataSize < 1) {
                    checksumsBuffer
                        .writeInt(0)
                    continue
                }

                val filePair = FilePair(indexId, archiveId)

                val byteBufSize = FilePair.SIZE_BYTES + 4 + dataSize
                val byteBuf = directBuffer(byteBufSize, byteBufSize)
                    .writeFilePair(filePair)
                    .writeInt(dataSize)
                    .writeBytes(data)

                val crc = CRC32().apply { update(data) }.value.toInt()
                checksumsBuffer.writeInt(crc)

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

        val checksumsBufferArray = ByteArray(checksumsBuffer.readableBytes())
        checksumsBuffer.readBytes(checksumsBufferArray)
        checksumsBuffer.release()

        val compressedChecksumsBufferArray = GzipCompression.compress(checksumsBufferArray)
        val compressedChecksumsBufferArraySize = compressedChecksumsBufferArray.size

        checksumsResponse = directBuffer(compressedChecksumsBufferArraySize + 4).run {
            writeInt(compressedChecksumsBufferArraySize)
            writeBytes(compressedChecksumsBufferArray)
            asReadOnly()
        }
    }

}