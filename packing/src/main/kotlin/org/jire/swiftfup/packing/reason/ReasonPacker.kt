package org.jire.swiftfup.packing.reason

import com.displee.cache.CacheLibrary
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.runelite.cache.ItemManager
import net.runelite.cache.fs.Store
import org.jire.swiftfup.packing.DefaultXteaRepository
import java.io.File
import java.nio.file.Path

object ReasonPacker {

    private const val COMPARE_CACHE_FROM_PATH = "data/osrs/cache204/"

    private const val CACHE_FROM_PATH = "data/osrs/cache221/"
    private const val CACHE_TO_PATH = "../server/cache/"

    private const val REBUILD = true
    private const val REBUILD_DIRECTORY_NAME = "rebuild"
    private const val REBUILD_DIRECTORY_PATH = "${CACHE_TO_PATH}$REBUILD_DIRECTORY_NAME"

    private const val MAP_INDEX_ID = 5

    private const val CONFIG_ARCHIVE_ID = 2

    @JvmStatic
    fun main(args: Array<String>) {
        //val compareFrom = CacheLibrary.create(COMPARE_CACHE_FROM_PATH)

        if (false) {
            Store(File(CACHE_FROM_PATH)).use { fromStore ->
                fromStore.load()

                val itemManager = ItemManager(fromStore)
                itemManager.load()

                val spellbookItemIds = itemManager.items.filter { it.params?.containsKey(596) == true }.map { it.id }
                println(spellbookItemIds.joinToString(","))
            }
        }

        val from = CacheLibrary.create(CACHE_FROM_PATH)
        val to = CacheLibrary.create(CACHE_TO_PATH)

        //val mismatchModels = compareIndex(compareFrom, to, 7)
        //val mismatchNpcs = compareConfigs(compareFrom, to, 9)
        //val mismatchItems = compareConfigs(compareFrom, to, 10)
        //println(compareFrom.index(2).archive(10)!!.fileIds().joinToString(","))
        //println(to.index(2).archive(10)!!.fileIds().joinToString(","))

        index(from, to, 7,
            skip(46540..46580).apply {
                addAll(skip(58907, 59521))
            }
        ) // models
        index(from, to, 0) // animations
        index(from, to, 1) // skeletons

        index(from, to, 8,
            skip(4510..4712).apply {
                add(423)
                add(155)
            }
        ) // sprites
        index(from, to, 9) // textures
        index(from, to, 17) // mapscene info

        configs(from, to, 1) // underlay
        configs(from, to, 4) // overlay

        configs(from, to, 12) // sequence
        configs(from, to, 6, skip(
            46241, 33410, 46240, 29241, 46243, 36582, 36552, 42820, 10961,
            11732, 11734, 11733, 11731,10068)
        ) // objects
        configs(from, to, 9, skip(
            8064, 8065, 8066, 20016, 763, 12004, 20017, 6477, 12007, 12008, 12009, 12005, 4058, 2989, 5523, 5527, 6481, 5906, 7456, 10631, 2914, 4753, 5792, 7958, 311, 1482, 2882, 3842, 1501, 2879, 8532, 10619, 12001, 12000, 7903, 11903,

            11895, 11896, 1787, 8262, 11747, 11749, 11756, 11757, 11758, 11760, 11762, 11764, 11812, 11844, 11850,
            11746, 11748, 11759, 11761, 11763, 11813, 11845, 11777, 11789, 11790, 11791, 11792, 11793, 11794,
            11795, 11796, 11797, 11798, 11799, 963, 965,

            4303, 4304, 6500, 6501, 7144, 7145, 7146, 7147, 7148, 7149, 7152, 9012, 9013, 9014, 9015, 9016, 9017, 9018, 9019, 9020, 9021,
            9022, 9023, 9024, 9035, 9036, 9037, 9038, 11279, 11280, 11281, 11282,

            11753, 11754, 11755, 11764)
        ) // npcs
        configs(from, to, 10, skip(
            620, 30461, 11738, 19473, 7478, 30460, 30458, 7968, 607, 30459, 608, 9625, 30611, 30461, 4810, 30446, 30462, 30448, 30449, 2528, 11862, 13343, 11847, 30457, 30456, 30455, 30453, 30463, 22092, 23987, 24125, 12791, 13226, 5509, 5510, 5512, 5514, 26784, 13639, 3839, 3841, 3843, 12607, 12609, 12611, 30612, 23983, 24123, 23991, 24127, 23673, 23680, 23762, 23862, 23863, 23864, 23971, 23975, 23979, 25424, 25426, 25430, 25432, 20997, 26374, 30548, 30587, 22622,  30515, 30625, 25910, 30545, 30510, 30511, 30512, 22542, 30563, 30566, 22552, 30561, 30565, 22547, 30562, 30568, 30550, 30520, 30547, 30507, 30546, 30506, 30554, 30505, 30551, 30508, 30552, 30509, 30516, 30553, 30514, 30502, 30503, 30504, 30594, 30622, 30380, 30583, 30549, 30518, 30585, 30517, 30584, 30586, 30519, 30624, 25910, 24444, 24370, 27374, 30608, 30187, 30602, 30601, 30603, 30599, 30598, 30597, 26372, 30591, 30595, 30590, 30477, 27241, 27238, 30468, 30467, 30466, 30497, 30464, 30623, 30620, 30619, 30621, 30618, 26225, 27235, 27232, 27229, 27226, 27246, 23083, 989, 23951, 23962, 30592, 30593, 30513, 30617, 30622, 30473, 30472, 30474, 30475, 26180, 26172, 26170, 26166, 26158, 26168, 26156, 23995, 23997, 24551, 25862, 25865, 25867, 2677, 2722, 2801, 12073, 19835, 23182, 3176, 22743, 12765, 8876, 8878, 8874, 8872,
            1741,2529,2885,3273,3274,3275,3276,3277,3278,3279,3280,3281,3282,3283,3284,3285,3286,3287,3288,3289,3290,3291,3292,3293,3294,3295,3296,3297,3298,3299,3300,3301,3302,3303,3304,3305,3306,3307,3308,3309,3310,3311,3312,3313,3314,3315,3316,3317,3318,3319,3320,3321,3322,3323,3324,4176,4553,4554,4555,4556,4629,4630,4631,4632,4633,4634,4635,4636,4637,4638,4639,4640,4641,4642,4643,4644,4645,4646,4647,4648,4649,4650,4651,4652,5000,6208,6567,6797,6884,6888,7619,8092,8796,8828,9107,9108,9109,9110,9111,9112,9113,9114,9115,9116,9117,9118,9119,9120,9121,9122,9123,9124,9125,9126,9127,9128,9129,9130,9131,9132,9133,9134,9135,9136,9137,9712,10511,11142,11143,11144,11145,11146,11147,11148,11149,11150,11997,15303,15304,15309,15344,15345,15346,17152,19475,20391,20392,20398,20399,20400,20404,20409,20410,20411,20412,20413,20414,20419,20420,20427,20589,20759,20762,20763,21826,21829,21832,21835,21836,21876,21877,21878,21879,22779,24369,24610,24612,25506,25507,25508,25509,25510,25511,25512,25513,25514,26553,26563,26564,26565,27089
        )) // items
        configs(from, to, 13) // spotanim
        //configs(from, to, 14) // varbit

        maps(from, to, skip = skip(
            12342, 4672, 8534, 4904
        ))

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
        indexId: Int,
        skip: IntSet? = null
    ) {
        val fromIndex = from.index(indexId).apply { cache() }
        val toIndex = to.index(indexId).apply { cache() }

        var amount = 0
        for (archive in fromIndex.archives()) {
            if (skip != null && skip.contains(archive.id)) continue

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
        skip: IntSet? = null,
        indexId: Int = CONFIG_ARCHIVE_ID
    ) {
        val fromIndex = from.index(indexId).apply { cache() }
        val toIndex = to.index(indexId).apply { cache() }

        var amount = 0

        val fromArchive = fromIndex.archive(configId)!!
        val toArchive = toIndex.archive(configId)!!

        for (file in fromArchive.files()) {
            if (skip != null && skip.contains(file.id)) continue
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
        skip: IntSet? = null,
        defaultXtea: IntArray = IntArray(4)
    ) {
        val fromXteas = DefaultXteaRepository.load(path = Path.of(CACHE_FROM_PATH, "xteas.json"))
        println("Loaded ${fromXteas.size} from xteas")

        val toXteas = DefaultXteaRepository.load(path = Path.of("data", "reason", "old-cache", "xteas.json"))
        println("Loaded ${toXteas.size} to xteas")

        var amount = 0
        for (region in 0..65535) {
            if (skip != null && skip.contains(region)) continue

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

    private fun skip(idRange: IntRange): IntSet {
        return IntOpenHashSet(idRange.toSet())
    }

    private fun skip(vararg ids: Int): IntSet {
        val set = IntOpenHashSet(ids.size)
        for (id in ids) {
            set.add(id)
        }
        return set
    }

}