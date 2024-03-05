package org.jire.swiftfup.packing.roatz

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index317
import io.netty.buffer.Unpooled
import java.io.File

object RoatzPacker {

    private const val CACHE_FROM_PATH = "data/osrs/cache216/"
    private const val CACHE_TO_PATH = "../server/cache/"
    private const val CACHE_ROATZ_PATH = "data/roatz/"
    private const val CACHE_ROATZ_CACHE_PATH = "${CACHE_ROATZ_PATH}cache/"

    private const val PACK_OLD_FORMAT = true
    private const val PACK_OSRS_DATA = true

    private const val PACK_OSRS_SPRITES_INDEX = true
    private const val OSRS_SPRITES_INDEX_ID = 6

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "$CACHE_TO_PATH$REBUILD_DIRECTORY_NAME"

    private const val SCAN_FOR_LARGE_FILES = false
    private const val SCAN_FOR_LARGE_FILES_MIN_BYTES = 8192

    private const val DUMP_CUSTOM_MAPS = true
    const val DUMP_CUSTOM_MAPS_PATH = "${CACHE_ROATZ_PATH}custom_maps/"

    val customRegionIds = intArrayOf(
        5169,
        5425,

        //551,
        4657,
        4907,
        4913,
        5163,
        5169,
        5419,
        5425,
        5675,
        5678,
        5679,
        5681,
        5789,
        5931,
        5934,
        5935,
        7228,
        7492,
        7741,
        7742,
        7997,
        7998,
        8781,
        9018,
        9043,
        9287,
        9512,
        9541,
        10024,
        10026,
        10042,
        10057,
        10058,
        10314,
        10549,
        10568,
        10826,
        11047,
        11048,
        11049,
        11061,
        11062,
        11303,
        11304,
        11305,
        11559,
        11560,
        11561,
        11562,
        11563,
        11589,
        11593,
        11815,
        11816,
        11817,
        11819,
        11828,
        12071,
        12072,
        12073,
        12088,
        12090,
        12102,
        12342,
        12605,
        12697,
        12889,
        13103,
        13133,
        13358,
        13362,
        13363,
        13364,
        13368,
        13370,
        13617,
        13642,
        13644,
        13660,
        13913,
        13916,
        14137,
        14170,
        14171,
        14393,
        14676,
        14904,
        14908
    )

    @JvmStatic
    fun main(args: Array<String>) {
        copyOriginalRoatzFiles()

        Index317.addMetaFiles("custom_sprites_version", "custom_sprites_crc")
        Index317.addMetaFiles("osrs_sprites_version", "osrs_sprites_crc")

        val cacheTo = CacheLibrary.create(CACHE_TO_PATH)

        if (SCAN_FOR_LARGE_FILES) {
            scanForLargeFiles(cacheTo)
            return
        }

        val cacheFrom = CacheLibrary.create(CACHE_FROM_PATH)

        if (DUMP_CUSTOM_MAPS) {
            dumpCustomMaps()
        }

        if (PACK_OSRS_SPRITES_INDEX) {
            packOsrsSprites(cacheFrom, cacheTo)
        }

        if (PACK_OSRS_DATA) RoatzOsrsDataPacker.pack(cacheFrom, cacheTo)

        if (PACK_OLD_FORMAT) RoatzOldFormatPacker.pack(CACHE_ROATZ_CACHE_PATH, cacheTo)

        cacheTo.update()
        cacheTo.close()

        cacheFrom.close()

        if (REBUILD) {
            rebuild()
        }
    }

    private fun copyOriginalRoatzFiles() {
        File(CACHE_TO_PATH).deleteRecursively()
        File(CACHE_TO_PATH).mkdirs()

        val roatzFiles = mutableListOf("main_file_cache.dat")
        for (i in 0..5) roatzFiles += "main_file_cache.idx$i"
        for (file in roatzFiles) File(CACHE_ROATZ_CACHE_PATH, file).copyTo(File(CACHE_TO_PATH, file))
    }

    private fun scanForLargeFiles(cacheTo: CacheLibrary) {
        for (index in cacheTo.indices()) {
            val indexId = index.id

            if (indexId != 0) {
                index.cache() // reads all archive data
            }

            val archives = index.archives()
            for (archive in archives) {
                val files = archive.files()
                for (file in files) {
                    val fileSize = file.data?.size ?: -1
                    if (fileSize >= SCAN_FOR_LARGE_FILES_MIN_BYTES)
                        println("index ${index.id}: ${archive.id}:${file.id} size is $fileSize")
                }
            }
        }
    }

    private fun dumpCustomMaps() {
        val cacheOriginal = CacheLibrary.create(CACHE_ROATZ_CACHE_PATH)
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

            val folder = "$DUMP_CUSTOM_MAPS_PATH$region/"
            val folderFile = File(folder)
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }

            val mapName = "${folder}m.dat"
            val landName = "${folder}l.dat"

            File(mapName).writeBytes(mapData)
            File(landName).writeBytes(landData)
        }
    }

    private fun packOsrsSprites(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexTo =
            if (cacheTo.exists(OSRS_SPRITES_INDEX_ID)) {
                cacheTo.index(OSRS_SPRITES_INDEX_ID).apply { clear() }
            } else {
                cacheTo.createIndex().also { cacheTo.reload() }
            }

        val indexFrom = cacheFrom.index(8)
        indexFrom.cache()

        for (archive in indexFrom.archives()) {
            if (!archive.containsData()) {
                println("archive ${archive.id} doesn't contain data! (has ${archive.files().size} files)")
                continue
            }

            for (file in archive.files()) {
                val data = file.data!!

                println("put ${archive.id}:${file.id} with ${data.size} bytes")
                cacheTo.put(OSRS_SPRITES_INDEX_ID, archive.id, file.id, data)
            }
        }

        indexTo.update()
    }

    private fun rebuild() {
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