package org.jire.swiftfup.packing.areos

import com.displee.cache.CacheLibrary
import java.io.File

object AreosPacker {

    private const val CACHE_FROM_PATH = "data/areos/cache_204/"
    private const val CACHE_TO_PATH = "data/areos/cache/"

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "${CACHE_TO_PATH}$REBUILD_DIRECTORY_NAME"

    @JvmStatic
    fun main(args: Array<String>) {
        val from = CacheLibrary.create(CACHE_FROM_PATH)
        val to = CacheLibrary.create(CACHE_TO_PATH)

        modIcons(from, to)

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

    private fun modIcons(from: CacheLibrary, to: CacheLibrary) {
        val fromIndex = from.index(8)
        fromIndex.cache()

        val toIndex = to.index(8)
        toIndex.cache()

        toIndex.add(fromIndex.archive(423))

        toIndex.update()
    }

}