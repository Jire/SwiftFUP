package org.jire.swiftfup.packing.reason

import com.displee.cache.CacheLibrary
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import org.jire.swiftfup.packing.DefaultXteaRepository
import java.io.File
import java.nio.file.Path

object ReasonPacker {

    private const val COMPARE_CACHE_FROM_PATH = "data/osrs/cache204/"

    private const val CACHE_FROM_PATH = "data/osrs/cache220/"
    private const val CACHE_TO_PATH = "../server/cache/"

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "${CACHE_TO_PATH}$REBUILD_DIRECTORY_NAME"

    private const val MAP_INDEX_ID = 5

    private const val CONFIG_ARCHIVE_ID = 2

    @JvmStatic
    fun main(args: Array<String>) {
        //val compareFrom = CacheLibrary.create(COMPARE_CACHE_FROM_PATH)

        val from = CacheLibrary.create(CACHE_FROM_PATH)
        val to = CacheLibrary.create(CACHE_TO_PATH)

        //val mismatchModels = compareIndex(compareFrom, to, 7)
        //val mismatchNpcs = compareConfigs(compareFrom, to, 9)
        //val mismatchItems = compareConfigs(compareFrom, to, 10)
        //println(compareFrom.index(2).archive(10)!!.fileIds().joinToString(","))
        //println(to.index(2).archive(10)!!.fileIds().joinToString(","))

        index(from, to, 7) // models
        index(from, to, 0) // animations
        index(from, to, 1) // skeletons

        configs(from, to, 6) // objects
        configs(from, to, 9) // npcs
        configs(from, to, 10) // items
        configs(from, to, 13) // spotanim
        configs(from, to, 14) // varbit

        maps(from, to)

        to.update()
        to.close()

        from.close()
        //compareFrom.close()

        if (REBUILD) {
            rebuild()
        }
    }

    fun index(
        from: CacheLibrary, to: CacheLibrary,
        indexId: Int
    ) {
        val fromIndex = from.index(indexId).apply { cache() }
        val toIndex = to.index(indexId).apply { cache() }

        var amount = 0
        for (archive in fromIndex.archives()) {
            if (archive.containsData()) {
                toIndex.add(archive)
                archive.flag()

                amount++
            }
        }

        toIndex.update()

        println("index $indexId added $amount archives")
    }

    fun configs(
        from: CacheLibrary, to: CacheLibrary,
        configId: Int,
        indexId: Int = CONFIG_ARCHIVE_ID
    ) {
        val fromIndex = from.index(indexId).apply { cache() }
        val toIndex = to.index(indexId).apply { cache() }

        var amount = 0
        val toArchive = toIndex.archive(configId)!!

        for (file in toArchive.files()) {
            file.data ?: continue
            toArchive.add(file)
            amount++
        }

        toArchive.flag()

        toIndex.update()

        println("index $indexId config $configId added $amount files")
    }

    fun maps(
        from: CacheLibrary, to: CacheLibrary,
        indexId: Int = MAP_INDEX_ID,
        defaultXtea: IntArray = IntArray(4)
    ) {
        val fromXteas = DefaultXteaRepository.load(path = Path.of(CACHE_FROM_PATH, "xteas.json"))
        println("Loaded ${fromXteas.size} from xteas")

        val toXteas = DefaultXteaRepository.load(path = Path.of("data", "reason", "old-cache", "xteas.json"))
        println("Loaded ${toXteas.size} to xteas")

        var amount = 0
        for (region in 0..65535) {
            val x = (region ushr 8) and 0xFF
            val y = region and 0xFF

            val mapName = "m${x}_$y"
            val mapData = from.data(indexId, mapName, 0) ?: continue

            val fromXtea = fromXteas.get(region)?.key ?: defaultXtea

            val landName = "l${x}_$y"
            val landData = from.data(indexId, landName, 0, fromXtea) ?: continue

            val toXtea = toXteas.get(region)?.key ?: defaultXtea

            to.put(indexId, mapName, mapData)
            to.put(indexId, landName, landData, toXtea)

            amount++

            //println("put region $region")
        }

        to.index(indexId).update()

        println("updated $amount map regions")
    }

    fun compareIndex(
        from: CacheLibrary, to: CacheLibrary,
        indexId: Int
    ): IntSet {
        val mismatchArchiveIds: IntSet = IntOpenHashSet()

        val fromIndex = from.index(indexId).apply { cache() }
        val toIndex = to.index(indexId).apply { cache() }

        for (archive in toIndex.archives()) {
            val archiveId = archive.id

            val dataFrom = from.data(indexId, archiveId) ?: continue
            val dataTo = to.data(indexId, archiveId)

            if (dataFrom.size != dataTo?.size) {
                mismatchArchiveIds.add(archiveId)
                //println("mismatch of $indexId:$archiveId (from: ${dataFrom.size}     to: ${dataTo?.size})")
            }
        }

        return mismatchArchiveIds
    }

    fun compareConfigs(
        from: CacheLibrary, to: CacheLibrary,
        configId: Int,
        indexId: Int = CONFIG_ARCHIVE_ID
    ): IntSet {
        val mismatchFileIds: IntSet = IntOpenHashSet()

        val fromIndex = from.index(indexId).apply { cache() }
        val toIndex = to.index(indexId).apply { cache() }

        val fromArchive = fromIndex.archive(configId)
        val toArchive = toIndex.archive(configId)!!

        for (file in toArchive.files()) {
            val fileId = file.id

            val dataFrom = from.data(indexId, configId, fileId) ?: continue
            val dataTo = to.data(indexId, configId, fileId)

            if (dataFrom.size != dataTo?.size) {
                mismatchFileIds.add(fileId)
                println("mismatch config of $indexId:$configId:$fileId (from: ${dataFrom.size}     to: ${dataTo?.size})")
            }
        }

        return mismatchFileIds
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