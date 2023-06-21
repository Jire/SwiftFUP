package org.jire.swiftfup.packing.roatz

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index317
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object RoatzPacker {

    private const val CACHE_FROM_PATH = "../server/cache214/"
    internal const val CACHE_TO_PATH = "../server/cache/"

    private const val PACK_OLD_FORMAT = true
    private const val PACK_214_DATA = true
    private const val PACK_TEXTURES = false

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "$CACHE_TO_PATH$REBUILD_DIRECTORY_NAME"

    private const val SCAN_FOR_LARGE_FILES = false
    private const val SCAN_FOR_LARGE_FILES_MIN_BYTES = 8192

    @JvmStatic
    fun main(args: Array<String>) {
        Index317.addMetaFiles("sounds_version", "sounds_crc")
        Index317.addMetaFiles("sprites_version", "sprites_crc")

        val cacheFrom = CacheLibrary.create(CACHE_FROM_PATH)
        val cacheTo = CacheLibrary.create(CACHE_TO_PATH)

        if (SCAN_FOR_LARGE_FILES) {
            for (index in cacheTo.indices()) {
                for (archive in cacheTo.index(index.id).archives()) {
                    for (file in archive.files()) {
                        val fileSize = file.data?.size ?: -1
                        if (fileSize >= SCAN_FOR_LARGE_FILES_MIN_BYTES)
                            println("index ${index.id}: ${archive.id}:${file.id} size is $fileSize")
                    }
                }
            }
            return
        }

        if (PACK_OLD_FORMAT) RoatzOldFormatPacker.pack(CACHE_TO_PATH, cacheTo)
        if (PACK_214_DATA) Roatz214DataPacker.pack(cacheFrom, cacheTo)

        if (PACK_TEXTURES) {
            cacheTo.put(0, 2, "textures.dat", Files.readAllBytes(Path.of("../server/textures.dat")))

            //println(cacheTo.index(0).archive(6)!!.files().joinToString(", "))
            cacheTo.index(0).archive(6)!!.clear()
            for (file in File("../server/pink").listFiles()!!) {
                val id = file.nameWithoutExtension.toInt()
                cacheTo.put(0, 6, id, file.readBytes())
            }

            cacheTo.index(0).update()
        }

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

}