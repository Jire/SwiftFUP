package org.jire.swiftfup.packing.roatz

import com.displee.cache.CacheLibrary
import io.netty.buffer.Unpooled
import org.jire.swiftfup.common.GzipCompression
import org.jire.swiftfup.packing.DefaultXteaRepository
import java.io.File

internal object RoatzOsrsDataPacker {

    private const val MODELS = true
    private const val ITEMS = true
    private const val NPCS = true
    private const val GRAPHICS = true
    private const val OBJECTS = true
    private const val MAPS = true
    private const val FRAME_BASES = true
    private const val FRAMES = true
    private const val ANIMATIONS = true
    private const val TEXTURES = true

    fun pack(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        if (MODELS) models(cacheFrom, cacheTo)
        if (ITEMS) items(cacheFrom, cacheTo)
        if (NPCS) npcs(cacheFrom, cacheTo)
        if (GRAPHICS) graphics(cacheFrom, cacheTo)
        if (OBJECTS) objects(cacheFrom, cacheTo)
        if (MAPS) maps(cacheFrom, cacheTo)
        if (FRAME_BASES) frameBases(cacheFrom, cacheTo)
        if (FRAMES) frames(cacheFrom, cacheTo)
        if (ANIMATIONS) animations(cacheFrom, cacheTo)
        if (TEXTURES) textures(cacheFrom, cacheTo)
    }

    private fun models(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(7)
        indexFrom.cache()

        val indexTo = cacheTo.index(1)
        //indexTo.clear()

        var count = 0
        for (archive in indexFrom.archives()) {
            if (!archive.containsData()) continue

            indexTo.remove(archive.id)
            indexTo.add(archive)

            count++
        }

        indexTo.update()

        println("models count $count")
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
            val data = file.data!!
            val dataSize = data.size
            if (dataSize < 1) {
                idx.writeShort(0)
                throw IllegalStateException("item skip ${file.id}")
            } else {
                val fileId = file.id
                if (dataSize >= 65535) throw IllegalStateException("TOO LARGE ITEM SIZE! $dataSize for ${fromArchive.id}:$fileId")

                idx.writeShort(dataSize)
                dat.writeBytes(data)

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

    private fun npcs(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1

        val idx = Unpooled.buffer()
        idx.writeShort(0)

        val dat = Unpooled.directBuffer()

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(9)!!
        for (file in fromArchive.files()) {
            val data = file.data!!
            val dataSize = data.size
            if (dataSize < 1) {
                idx.writeShort(0)
                throw IllegalStateException("NPC skip ${file.id}")
            } else {
                val fileId = file.id

                if (dataSize >= 65535) throw IllegalStateException("TOO LARGE NPC size $dataSize for ID $fileId")
                idx.writeShort(dataSize)
                dat.writeBytes(data)

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

    private fun graphics(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(13)!!
        for (file in fromArchive.files()) {
            val data = file.data!!
            val dataSize = data.size
            if (dataSize < 1) {
                println("skipped spotanim file ${file.id} (no data)")
                buf.writeShort(-1)
            } else {
                val fileId = file.id
                if (dataSize >= 65535) throw IllegalStateException("TOO LARGE GRAPHIC! ${fromArchive.id}:$fileId size was $dataSize")
                buf.writeShort(fileId)
                buf.writeShort(dataSize)
                buf.writeBytes(data)
                if (fileId > highestFileId)
                    highestFileId = fileId
                if (dataSize > biggestSize)
                    biggestSize = dataSize
            }
        }
        buf.writeShort(-1)

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
            val data = file.data!!
            val dataSize = data.size
            if (dataSize < 1) {
                idx.writeShort(0)
                throw IllegalStateException("object skip ${file.id}")
            } else {
                val fileId = file.id
                if (dataSize >= 65535) throw IllegalStateException("TOO LARGE OBJECT SIZE $dataSize for ${fromArchive.id}:$fileId")
                idx.writeShort(dataSize)
                dat.writeBytes(data)

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

    private fun maps(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val idx = Unpooled.buffer()
        idx.writeShort(0)

        var mapCount = 0
        var fileId = 0

        for (region in RoatzPacker.customRegionIds) {
            val mapFileId = fileId++
            val landFileId = fileId++

            val x = (region ushr 8) and 0xFF
            val y = region and 0xFF

            val mapName = "${RoatzPacker.DUMP_CUSTOM_MAPS_PATH}m${x}_$y"
            val landName = "${RoatzPacker.DUMP_CUSTOM_MAPS_PATH}l${x}_$y"

            val mapData = File(mapName).readBytes()
            val landData = File(landName).readBytes()

            cacheTo.remove(4, mapFileId)
            cacheTo.put(4, mapFileId, mapData)

            cacheTo.remove(4, landFileId)
            cacheTo.put(4, landFileId, landData)

            idx.writeShort(region)
            idx.writeShort(mapFileId)
            idx.writeShort(landFileId)

            mapCount++

            println("for custom region $region ($x,$y) map=$mapFileId and land=$landFileId")
        }

        val xteas = DefaultXteaRepository.load()
        val defaultXtea = intArrayOf(0, 0, 0, 0)
        for (region in 0..65535) {
            if (RoatzPacker.customRegionIds.contains(region)) continue

            val x = (region ushr 8) and 0xFF
            val y = region and 0xFF

            val mapName = "m${x}_$y"
            val mapData = cacheFrom.data(5, mapName, 0) ?: continue

            val landName = "l${x}_$y"
            val landData = cacheFrom.data(5, landName, 0, xteas.get(region)?.key ?: defaultXtea) ?: continue

            val mapFileId = fileId++
            val landFileId = fileId++

            cacheTo.remove(4, mapFileId)
            cacheTo.put(4, mapFileId, mapData)

            cacheTo.remove(4, landFileId)
            cacheTo.put(4, landFileId, landData)

            idx.writeShort(region)
            idx.writeShort(mapFileId)
            idx.writeShort(landFileId)

            mapCount++

            println("for region $region ($x,$y) map=$mapFileId and land=$landFileId")
        }

        idx.setShort(0, mapCount)

        val idxArray = ByteArray(idx.writerIndex())
        idx.readBytes(idxArray)

        val compressedArray = GzipCompression.compress(idxArray)

        cacheTo.put(0, 5, "map_index", compressedArray)

        cacheTo.index(0).update()
        cacheTo.index(4).update()
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

    private fun animations(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
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
            }
        }

        buf.setShort(0, highestFileId)

        buf.readerIndex(0)
        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)

        val compressedArray = GzipCompression.compress(array)

        println("seq highest $highestFileId and biggest was $biggestSize (total bytes=${array.size}, compressed bytes=${compressedArray.size})")

        cacheTo.put(0, 2, "seq.dat", compressedArray)

        cacheTo.index(0).update()
    }

    private fun textures(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val fromTexturesIndex = cacheFrom.index(9)
        fromTexturesIndex.cache()

        val idx = Unpooled.buffer()
        var highestFileId = 0
        idx.writeShort(highestFileId) // placeholder

        val archive = fromTexturesIndex.archive(0)!!
        for (file in archive.files()) {
            val id = file.id
            val data = file.data!!
            val dataSize = data.size
            if (dataSize >= 65535)
                throw IllegalStateException("Too large texture data for file $id (size=$dataSize)")

            idx.writeShort(id)
            idx.writeShort(dataSize)
            idx.writeBytes(data)

            //cacheTo.put(0, 6, "${id}.dat", data)

            if (id > highestFileId) {
                highestFileId = id
            }
        }

        idx.setShort(0, highestFileId)
        val idxArray = ByteArray(idx.readableBytes())
        idx.readBytes(idxArray)
        idx.release()

        cacheTo.put(0, 2, "textures.dat", idxArray)

        cacheTo.index(0).update()

        println("packed textures highest=$highestFileId")
    }

}