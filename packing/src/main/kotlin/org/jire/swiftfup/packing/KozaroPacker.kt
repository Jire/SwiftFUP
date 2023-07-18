package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.runelite.cache.definitions.loaders.SequenceLoader
import org.jire.swiftfup.common.GzipCompression
import java.io.File

object KozaroPacker {

    private const val CACHE_FROM_PATH = "../server/cache214/"
    internal const val CACHE_TO_PATH = "../server/cache/"

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "${CACHE_TO_PATH}$REBUILD_DIRECTORY_NAME"

    private val skeleIds: IntSet = IntOpenHashSet()

    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = CACHE_TO_PATH

        val cacheFrom = CacheLibrary.create(CACHE_FROM_PATH)
        val cacheTo = CacheLibrary.create(cachePath)

        seq(cacheFrom, cacheTo)
        frameBases(cacheFrom, cacheTo)
        frames(cacheFrom, cacheTo)

        cacheTo.update()
        cacheTo.close()

        cacheFrom.close()

        if (REBUILD) {
            val rebuildFile = File(REBUILD_DIRECTORY_PATH)
            if (rebuildFile.exists()) {
                rebuildFile.listFiles()?.forEach {
                    if (!it.delete()) {
                        throw IllegalStateException("Failed to delete rebuild directory file \"${it.name}\"")
                    }
                }
            } else if (!rebuildFile.mkdirs()) {
                throw IllegalStateException("Failed to create rebuild directory \"${REBUILD_DIRECTORY_PATH}\"")
            }

            CacheLibrary.create(CACHE_TO_PATH).rebuild(rebuildFile)
        }
    }

    private fun seq(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(12)!!
        for (file in fromArchive.files()) {
            val data = file.data!!
            val dataSize = data.size
            if (dataSize < 1) {
                throw IllegalStateException("skipped seq file ${file.id} (no data)")
                buf.writeShort(-1)
            } else {
                val fileId = file.id
                buf.writeShort(fileId)
                if (dataSize >= 65535) throw IllegalStateException("TOO LARGE DATA! ${fromArchive.id}:$fileId size $dataSize")
                buf.writeShort(dataSize)
                buf.writeBytes(data)
                if (fileId > highestFileId)
                    highestFileId = fileId
                if (data.size > biggestSize)
                    biggestSize = data.size

                //println("seq $fileId length ${data.size}")

                val def = SequenceLoader().load(fileId, data)
                val skeleId = def.animMayaID
                if (skeleId != -1) {
                    val skeleGroupId = skeleId ushr 16
                    //System.err.println("file ${file.id} had def $skeleId vs $skeleGroupId")
                    skeleIds.add(skeleGroupId)
                }
            }

        }

        buf.setShort(0, highestFileId)

        buf.readerIndex(0)
        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)

        println("seq highest $highestFileId and biggest was $biggestSize (total bytes=${array.size})")

        cacheTo.put(0, 2, "seq.dat", array)

        cacheTo.index(0).update()
    }

    private fun frameBases(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(1)
        indexFrom.cache()

        val buf = Unpooled.buffer()
        var count = 0
        buf.writeShort(count) // placeholder

        for (archive in indexFrom.archives()) {
            val groupId = archive.id
            if (!archive.containsData()) throw IllegalStateException("MUST HAVE DATA! $groupId")
            val file = archive.file(0)!!
            val data = file.data!!
            val dataSize = data.size
            if (dataSize >= 65535) throw IllegalStateException("TOO LARGE DATA! $groupId size $dataSize")
            buf.writeShort(groupId)
            buf.writeShort(dataSize)
            buf.writeBytes(data)
            count++
        }

        buf.setShort(0, count)

        buf.readerIndex(0)
        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)

        val compressedArray = GzipCompression.compress(array)

        cacheTo.put(0, 2, "framebases.dat", compressedArray)

        cacheTo.index(0).update()

        println("frame bases count $count and raw size ${array.size}, compressed size ${compressedArray.size}")
    }

    private fun frames(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(0)
        indexFrom.cache()

        val indexTo = cacheTo.index(2)
        indexTo.clear()

        var count = 0
        for (archive in indexFrom.archives()) {
            if (!archive.containsData()) continue

            val buf = Unpooled.buffer()
            var highestFileId = 0
            buf.writeShort(highestFileId) // placeholder

            for (file in archive.files()) {
                val fileId = file.id
                val data = file.data!!
                buf.writeShort(fileId)
                buf.writeMedium(data.size)
                buf.writeBytes(data)
                if (fileId > highestFileId)
                    highestFileId = fileId
            }

            buf.setShort(0, highestFileId)

            buf.readerIndex(0)
            val array = ByteArray(buf.readableBytes())
            buf.readBytes(array)

            cacheTo.put(2, archive.id, array)

            count++
        }

        indexTo.update()

        println("frames count $count")
    }

}