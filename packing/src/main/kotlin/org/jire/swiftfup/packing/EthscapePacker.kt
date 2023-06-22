package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index317
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Jire
 */
object EthscapePacker {

    private const val WINDOWS_USERNAME = "Administrator"
    private const val CACHE_PATH = "C:\\Users\\$WINDOWS_USERNAME\\.ethscape\\cache\\"

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "$CACHE_PATH$REBUILD_DIRECTORY_NAME"

    private const val DUMP_CUSTOM_MAPS = true
    const val DUMP_CUSTOM_MAPS_PATH = "ethscape_custom_maps/"

    val customRegionIds = intArrayOf(
        7991, 7992, 7993, 8249, 8250, 8248, 8247, 8504, 8527, 8528, 10537, 7480, 10569
    )

    @JvmStatic
    fun main(args: Array<String>) {
        Index317.addMetaFiles("anims717_version", "anims717_crc")
        Index317.addMetaFiles("ethsprites_version", "ethsprites_crc")
        Index317.addMetaFiles("osrssprites_version", "osrssprites_crc")

        val cacheTo = CacheLibrary.create(CACHE_PATH)

        cacheTo.mainFileSprites(CACHE_PATH)

        val cacheFrom = CacheLibrary.create("../server/cache213")

        if (DUMP_CUSTOM_MAPS) {
            val cacheOriginal = CacheLibrary.create("../server/cache-ethscape-original/")
            val data = cacheOriginal.data(0, 5, "map_index")
            val buf = Unpooled.wrappedBuffer(data)
            val count = buf.readUnsignedShort()
            repeat(count) {
                val region = buf.readUnsignedShort()

                val mapFileId = buf.readUnsignedShort()
                val landFileId = buf.readUnsignedShort()

                if (!customRegionIds.contains(region)) return@repeat

                val mapData = cacheOriginal.data(4, mapFileId)!!
                val landData = cacheOriginal.data(4, landFileId)!!

                val x = (region ushr 8) and 0xFF
                val y = region and 0xFF

                val mapName = "${DUMP_CUSTOM_MAPS_PATH}m${x}_$y"
                val landName = "${DUMP_CUSTOM_MAPS_PATH}l${x}_$y"

                File(mapName).writeBytes(mapData)
                File(landName).writeBytes(landData)
            }
        }

        models(cacheFrom, cacheTo)
        items(cacheFrom, cacheTo)
        seq(cacheFrom, cacheTo)
        frameBases(cacheFrom, cacheTo)
        frames(cacheFrom, cacheTo)
        npc(cacheFrom, cacheTo)
        graphic(cacheFrom, cacheTo)
        objects(cacheFrom, cacheTo)
        varp(cacheFrom, cacheTo)
        varbit(cacheFrom, cacheTo)
        maps(cacheFrom, cacheTo)
        underlays(cacheFrom, cacheTo)
        overlays(cacheFrom, cacheTo)

        cacheTo.update()
        cacheTo.close()

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

            CacheLibrary.create(CACHE_PATH).rebuild(rebuildFile)
        }
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

        val customModelArchives: Int2ObjectMap<ByteArray> = Int2ObjectOpenHashMap()
        val customModelFiles = File("${CACHE_PATH}index1").listFiles()
        if (customModelFiles != null) {
            for (file in customModelFiles) {
                if (file.extension != "gz") continue

                val archiveId = file.nameWithoutExtension.toInt()
                val data = file.readBytes()
                customModelArchives.put(archiveId, data)

                cacheTo.remove(1, archiveId)
                cacheTo.put(1, archiveId, data)

                if (archiveId > highestId)
                    highestId = archiveId
            }
        }

        for (archive in indexFrom.archives()) {
            val archiveId = archive.id

            if (customModelArchives.containsKey(archiveId)) continue
            if (!archive.containsData()) continue

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

    private fun varbit(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(14)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null || data.size < 1) {
                println("skipped varbit file ${file.id} (no data)")
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

        println("varbit highest $highestFileId and biggest was $biggestSize (total bytes=${array.size})")

        cacheTo.put(0, 2, "varbit.dat", array)

        cacheTo.index(0).update()
    }

    private fun varp(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(16)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null || data.size < 1) {
                println("skipped varp file ${file.id} (no data)")
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

        println("varp highest $highestFileId and biggest was $biggestSize (total bytes=${array.size})")

        cacheTo.put(0, 2, "varp.dat", array)

        cacheTo.index(0).update()
    }

    private fun maps(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val idx = Unpooled.buffer()
        idx.writeShort(0)

        var mapCount = 0
        var fileId = 0

        for (region in customRegionIds) {
            val mapFileId = fileId++
            val landFileId = fileId++

            val x = (region ushr 8) and 0xFF
            val y = region and 0xFF

            val mapName = "${DUMP_CUSTOM_MAPS_PATH}m${x}_$y"
            val landName = "${DUMP_CUSTOM_MAPS_PATH}l${x}_$y"

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

        DefaultXteaRepository.load()
        for ((region, xtea) in DefaultXteaRepository.map.int2ObjectEntrySet()) {
            if (customRegionIds.contains(region)) continue

            val mapFileId = fileId++
            val landFileId = fileId++

            val x = (region ushr 8) and 0xFF
            val y = region and 0xFF

            val mapName = "m${x}_$y"
            val landName = "l${x}_$y"

            val mapData = cacheFrom.data(5, mapName, 0)!!
            val landData = cacheFrom.data(5, landName, 0, xtea.key)!!

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

        cacheTo.put(0, 5, "map_index", idxArray)

        cacheTo.index(0).update()
        cacheTo.index(4).update()
    }

    private fun underlays(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(1)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null || data.size < 1) {
                println("skipped underlay file ${file.id} (no data)")
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

        println("underlays highest $highestFileId and biggest was $biggestSize (total bytes=${array.size})")

        cacheTo.put(0, 2, "underlays.dat", array)

        cacheTo.index(0).update()
    }

    private fun overlays(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        var highestFileId = -1
        var biggestSize = 0
        val buf = Unpooled.buffer()
        buf.writeShort(0)

        val configIndex = cacheFrom.index(2)
        configIndex.cache()

        val fromArchive = configIndex.archive(4)!!
        for (file in fromArchive.files()) {
            val data = file.data
            if (data == null || data.size < 1) {
                println("skipped overlay file ${file.id} (no data)")
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

        println("overlays highest $highestFileId and biggest was $biggestSize (total bytes=${array.size})")

        cacheTo.put(0, 2, "overlays.dat", array)

        cacheTo.index(0).update()
    }

}