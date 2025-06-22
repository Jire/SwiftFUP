package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import java.io.File
import java.nio.file.Path

/**
 * @author Jire
 */
object DeXTEAPacker {

    private const val CACHE_VERSION = 228
    private const val CACHE_DIR = "cache$CACHE_VERSION"

    private const val CACHE_FROM_PATH = "data/osrs/$CACHE_DIR/"
    private const val CACHE_TO_PATH = "data/dextea/$CACHE_DIR/"

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "${CACHE_TO_PATH}$REBUILD_DIRECTORY_NAME"

    private const val MAP_INDEX_ID = 5

    @JvmStatic
    fun main(args: Array<String>) {
        val xteas = DefaultXteaRepository.load(Path.of("data", "osrs", CACHE_DIR, "xteas.json"))

        val indexId = MAP_INDEX_ID

        val from = CacheLibrary.create(CACHE_FROM_PATH)
        val to = CacheLibrary.create(CACHE_TO_PATH)

        val defaultLandData = ByteArray(0)
        val defaultXtea = IntArray(4)

        var amount = 0
        for (regionId in 0..65535) {
            val x = (regionId ushr 8) and 0xFF
            val y = regionId and 0xFF

            val mapName = "m${x}_$y"
            val mapData = from.data(indexId, mapName, 0) ?: continue

            val landName = "l${x}_$y"

            val xtea = xteas[regionId]
            if (xtea == null) {
                to.remove(indexId, mapName)
                to.remove(indexId, landName)
            } else {
                //to.put(indexId, mapName, mapData)

                val landData = from.data(indexId, landName, 0, xtea.key)!!
                to.put(indexId, landName, landData, defaultXtea)
            }

            amount++
        }

        to.index(indexId).update()

        println("Removed XTEAs from $amount map regions")

        to.update()
        to.close()

        from.close()

        if (REBUILD) {
            rebuild()
        }
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