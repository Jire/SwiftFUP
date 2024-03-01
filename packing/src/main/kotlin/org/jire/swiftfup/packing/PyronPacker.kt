package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import io.netty.buffer.Unpooled
import java.io.File

object PyronPacker {

    private const val REBUILD = true

    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = "../server/cache/"

        val cacheFrom = CacheLibrary.create("data/osrs/cache215/")
        val cacheTo = CacheLibrary.create(cachePath)

        models(cacheFrom, cacheTo)
        customModels(cacheTo)
        objects(cacheFrom, cacheTo)

        cacheTo.update()
        cacheTo.close()

        cacheFrom.close()

        if (REBUILD) {
            CacheLibrary.create(cachePath).rebuild(File("${cachePath}rebuild/"))
        }
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

    private fun models(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(7)
        indexFrom.cache()

        val indexTo = cacheTo.index(1)
        indexTo.clear()
        indexTo.cache()

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

    private fun customModels(cacheTo: CacheLibrary, customModelPath: String = "data/pyron/custom_models/") {
        val indexTo = cacheTo.index(1)

        val modelIds = File(customModelPath).listFiles()!!.map { it.name.toInt() }
        for (modelId in modelIds) {
            val data = File("$customModelPath$modelId").readBytes()
            cacheTo.put(1, modelId, data)
        }

        indexTo.update()

        println("packed ${modelIds.size} custom models")
    }

    private fun modelsNew(cacheFrom: CacheLibrary, cacheTo: CacheLibrary) {
        val indexFrom = cacheFrom.index(7)
        indexFrom.cache()

        val indexTo = cacheTo.index(1)
        indexTo.clear()
        indexTo.cache()

        var count = 0
        for (archive in indexFrom.archives()) {
            if (!archive.containsData()) continue

            val osrsData = cacheFrom.data(7, archive.id) ?: continue
            val bufOsrs = Unpooled.wrappedBuffer(osrsData)

            val model = Model.decode(archive.id, bufOsrs)
            model.removeSkeletalInformation()
            val buf317 = model.encode()

            val data317 = ByteArray(buf317.readableBytes())
            buf317.readBytes(data317)
            buf317.release()

            bufOsrs.release()

            cacheTo.remove(1, archive.id)
            cacheTo.put(1, archive.id, 0, data317)

            println("model ${archive.id} had ${data317.size} bytes")

            count++
        }

        indexTo.update()

        println("models count $count")
    }

}