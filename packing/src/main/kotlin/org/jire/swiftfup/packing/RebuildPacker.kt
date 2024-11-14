package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import java.io.File

/**
 * @author Jire
 */
object RebuildPacker {

    private const val CACHE_PATH = "data/cache-rebuild/"

    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "${CACHE_PATH}$REBUILD_DIRECTORY_NAME"

    @JvmStatic
    fun main(args: Array<String>) {
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
