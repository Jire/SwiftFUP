package org.jire.swiftfup.packing.roatz

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index317
import java.io.File

object RoatzPacker {

    private const val PACK_OLD_FORMAT = false
    private const val PACK_214_DATA = true
    private const val REBUILD = false

    @JvmStatic
    fun main(args: Array<String>) {
        Index317.addMetaFiles("sounds_version", "sounds_crc")
        Index317.addMetaFiles("sprites_version", "sprites_crc")

        val cachePath = "../server/cache/"
        val cacheFrom = CacheLibrary.create("../server/cache214/")
        val cacheTo = CacheLibrary.create(cachePath)

        if (PACK_OLD_FORMAT) RoatzOldFormatPacker.pack(cachePath, cacheTo)
        if (PACK_214_DATA) Roatz214DataPacker.pack(cacheFrom, cacheTo)

        cacheTo.update()
        cacheTo.close()

        cacheFrom.close()

        if (REBUILD) {
            val rebuildDir = File("${cachePath}../rebuild/")
            rebuildDir.walkTopDown().forEach { it.delete() }
            CacheLibrary.create(cachePath).rebuild(rebuildDir)
        }
    }

}