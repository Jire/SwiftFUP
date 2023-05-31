package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index317
import io.netty.buffer.Unpooled
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Jire
 */
object EthscapePacker {

    private const val REBUILD = false

    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = "C:\\Users\\Administrator\\.ethscape\\cache\\"

        Index317.addMetaFiles("anims717_version", "anims717_crc")
        Index317.addMetaFiles("ethsprites_version", "ethsprites_crc")
        Index317.addMetaFiles("osrssprites_version", "osrssprites_crc")

        val cacheTo = CacheLibrary.create(cachePath)
        if (REBUILD) {
            cacheTo.rebuild(File("${cachePath}rebuild"))
            return
        }

        cacheTo.mainFileSprites(cachePath)

        val cacheFrom = CacheLibrary.create("cache213")

        models(cacheFrom, cacheTo)
        items(cacheFrom, cacheTo)
        seq(cacheFrom, cacheTo)
        frameBases(cacheFrom, cacheTo)
        frames(cacheFrom, cacheTo)
        npc(cacheFrom, cacheTo)
        graphic(cacheFrom, cacheTo)
        objects(cacheFrom, cacheTo)


        cacheTo.update()
        cacheTo.close()
    }

    fun CacheLibrary.mainFileSprites(cachePath: String) {
        put(
            0, 9, "main_file_sprites.dat",
            Files.readAllBytes(Path.of(cachePath, "main_file_sprites.dat"))
        )
        put(
            0, 9, "main_file_sprites.idx",
            Files.readAllBytes(Path.of(cachePath, "main_file_sprites.idx"))
        )
        index(0).update()

        println("packed main_file_sprites")
    }

    fun models(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(7)
        indexFrom.cache()

        val indexTo = cacheTo.index(1)

        var highestId = 0
        for (archive in indexFrom.archives()) {
            if (!archive.containsData()) continue
            val archiveId = archive.id
            val data = cacheFrom.data(7, archiveId, 0) ?: continue
            cacheTo.remove(1, archiveId)
            cacheTo.put(1, archiveId, data)

            if (archiveId > highestId)
                highestId = archiveId
        }

        indexTo.update()

        println("packed models highest $highestId")
    }

    private fun items(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1

        val idx = Unpooled.buffer()
        idx.writeShort(0)

        val dat = Unpooled.directBuffer()

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(10)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null) {
                idx.writeShort(0)
                System.err.println("item skip ${file.id}")
            } else {
                idx.writeShort(data.size)
                dat.writeBytes(data)

                val fileId = file.id
                //System.err.println("packed NPC $fileId size ${data.size}")
                if (fileId > highestFileId) {
                    highestFileId = fileId
                }
            }
        }

        idx.writeShort(-1) // EOF
        idx.setShort(0, highestFileId)

        dat.readerIndex(0)
        val datArray = ByteArray(dat.readableBytes())
        dat.readBytes(datArray)

        idx.readerIndex(0)
        val idxArray = ByteArray(idx.readableBytes())
        idx.readBytes(idxArray)

        cacheTo.put(0, 2, "obj.dat", datArray)
        cacheTo.put(0, 2, "obj.idx", idxArray)

        cacheTo.index(0).update()

        println("item highest $highestFileId")
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
            val data = file.data
            if (data == null || data.size < 1) {
                println("skipped seq file ${file.id} (no data)")
                buf.writeShort(-1)
            } else {
                val fileId = file.id
                buf.writeShort(fileId)
                buf.writeShort(data.size)
                buf.writeBytes(data)
                if (fileId > highestFileId)
                    highestFileId = fileId
                if (data.size > biggestSize)
                    biggestSize = data.size

                //println("seq $fileId length ${data.size}")
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
        var highestId = 0
        buf.writeShort(highestId) // placeholder

        for (archive in indexFrom.archives()) {
            val groupId = archive.id
            if (!archive.containsData()) throw IllegalStateException("MUST HAVE DATA! $groupId")
            val file = archive.file(0)!!
            val data = file.data!!
            buf.writeShort(groupId)
            buf.writeShort(data.size)
            buf.writeBytes(data)
            if (groupId > highestId)
                highestId = groupId
        }

        buf.setShort(0, highestId)

        buf.readerIndex(0)
        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)

        cacheTo.put(0, 10, "framebases.dat", array)

        cacheTo.index(0).update()

        println("frame bases count $highestId and size ${array.size}")
    }

    private fun frames(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(0)
        indexFrom.cache()

        val indexTo = cacheTo.index(2)

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

            val archiveId = archive.id
            cacheTo.remove(2, archiveId)
            cacheTo.put(2, archiveId, array)

            count++
        }

        indexTo.update()

        println("frames count $count")
    }

    private fun npc(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1

        val idx = Unpooled.buffer()
        idx.writeShort(0)

        val dat = Unpooled.directBuffer()

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(9)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null) {
                idx.writeShort(0)
                System.err.println("NPC skip ${file.id}")
            } else {
                idx.writeShort(data.size)
                dat.writeBytes(data)

                val fileId = file.id
                //System.err.println("packed NPC $fileId size ${data.size}")
                if (fileId > highestFileId) {
                    highestFileId = fileId
                }
            }
        }

        idx.writeShort(-1) // EOF
        idx.setShort(0, highestFileId)

        dat.readerIndex(0)
        val datArray = ByteArray(dat.readableBytes())
        dat.readBytes(datArray)

        idx.readerIndex(0)
        val idxArray = ByteArray(idx.readableBytes())
        idx.readBytes(idxArray)

        cacheTo.put(0, 2, "npc.dat", datArray)
        cacheTo.put(0, 2, "npc.idx", idxArray)

        cacheTo.index(0).update()

        println("npc highest $highestFileId")
    }

    private fun graphic(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(13)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null || data.size < 1) {
                println("skipped spotanim file ${file.id} (no data)")
                buf.writeShort(-1)
            } else {
                val fileId = file.id
                buf.writeShort(fileId)
                buf.writeShort(data.size)
                buf.writeBytes(data)
                if (fileId > highestFileId)
                    highestFileId = fileId
                if (data.size > biggestSize)
                    biggestSize = data.size
            }
        }

        buf.setShort(0, highestFileId)

        buf.readerIndex(0)
        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)

        println("spotanim highest $highestFileId and biggest was $biggestSize (total bytes=${array.size})")

        cacheTo.put(0, 2, "spotanim.dat", array)

        cacheTo.index(0).update()
    }

    private fun objects(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1

        val idx = Unpooled.buffer()
        idx.writeShort(0)

        val dat = Unpooled.directBuffer()

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(6)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null) {
                idx.writeShort(0)
                System.err.println("object skip ${file.id}")
            } else {
                idx.writeShort(data.size)
                dat.writeBytes(data)

                val fileId = file.id
                if (fileId > highestFileId) {
                    highestFileId = fileId
                }
            }
        }

        idx.writeShort(-1) // EOF
        idx.setShort(0, highestFileId)

        dat.readerIndex(0)
        val datArray = ByteArray(dat.readableBytes())
        dat.readBytes(datArray)

        idx.readerIndex(0)
        val idxArray = ByteArray(idx.readableBytes())
        idx.readBytes(idxArray)

        cacheTo.put(0, 2, "loc.dat", datArray)
        cacheTo.put(0, 2, "loc.idx", idxArray)

        cacheTo.index(0).update()

        println("object highest $highestFileId")
    }

}