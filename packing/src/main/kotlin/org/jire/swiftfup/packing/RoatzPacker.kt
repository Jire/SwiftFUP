package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index317
import io.netty.buffer.Unpooled
import java.io.File

object RoatzPacker {

    private const val REBUILD = true

    @JvmStatic
    fun main(args: Array<String>) {
        Index317.addMetaFiles("sounds_version", "sounds_crc")
        Index317.addMetaFiles("sprites_version", "sprites_crc")

        val cachePath = "C:/Users/Administrator/roatpkzv6/"//"../server/cache/"
        val cacheFrom = CacheLibrary.create("../server/cache213/")
        val cacheTo = CacheLibrary.create(cachePath)

        if (false) {
            println(cacheTo.index(0).archives().joinToString(","))
            println(cacheTo.data(0, 9, "ClanVault/vault.png")!!.size)
            println(cacheTo.index(0).archive(9)!!.fileIds().joinToString(","))
            return
        }
        if (REBUILD) {
            val rebuildDir = File("${cachePath}../rebuild/")
            rebuildDir.walkTopDown().forEach { it.delete() }
            cacheTo.rebuild(rebuildDir)
            return
        }

        sounds(cachePath, cacheTo)
        sprites(cachePath, cacheTo)
        data(cachePath, cacheTo)

        cacheTo.update()
        cacheTo.close()

        cacheFrom.close()
    }

    private fun sounds(cachePath: String, cacheTo: CacheLibrary) {
        val index = if (cacheTo.exists(5)) cacheTo.index(5).apply { clear() }
        else cacheTo.createIndex()

        val indexBuf = Unpooled.buffer()
        indexBuf.writeShort(0)

        var amount = 0
        for (soundFile in File("${cachePath}Sounds2").listFiles()!!) {
            val id = soundFile.nameWithoutExtension.toInt()
            val data = soundFile.readBytes()

            indexBuf.writeShort(id)
            cacheTo.put(5, id, data)
            amount++
        }

        indexBuf.setShort(0, amount)

        val indexBufArray = ByteArray(indexBuf.writerIndex())
        indexBuf.readBytes(indexBufArray)

        cacheTo.put(0, 5, "sounds_index", indexBufArray)

        index.update()
    }

    private fun sprites(cachePath: String, cacheTo: CacheLibrary) {
        val index = if (cacheTo.exists(6)) cacheTo.index(6).apply { clear() }
        else cacheTo.createIndex()

        val indexBuf = Unpooled.buffer()
        var amount = 0
        indexBuf.writeShort(amount)

        val dirPath = "${cachePath}Sprites2/"
        println("dirPath: \"$dirPath\"")
        File(dirPath)
            .walkTopDown()
            .forEach { file ->
                if (!file.isFile) return@forEach

                if (".DS_Store" == file.name) {
                    file.delete()
                    return@forEach
                }
                val data = file.readBytes()
                val cacheFileName = file.path.replace('\\', '/').replace(dirPath, "")
                println("\"$cacheFileName\"")
                //cacheTo.put(0, 9, cacheFileName, data)
                val archive = cacheTo.put(6, cacheFileName, data)

                println("archive ID was ${archive.id} and hashName is ${archive.hashName}")
                indexBuf.writeShort(archive.id)
                indexBuf.writeInt(archive.hashName)

                amount++
            }

        indexBuf.setShort(0, amount)

        val array = ByteArray(indexBuf.writerIndex())
        indexBuf.readBytes(array)

        cacheTo.put(0, 5, "sprites_index", array)
        cacheTo.index(0).update()

        index.update()
    }

    private fun data(cachePath: String, cacheTo: CacheLibrary) {
        val index = cacheTo.index(0)

        val dirPath = "${cachePath}Data2/"
        File(dirPath)
            .walkTopDown()
            .forEach { file ->
                if (!file.isFile) return@forEach

                if (".DS_Store" == file.name || "ini" == file.extension) {
                    file.delete()
                    return@forEach
                }
                val cacheFileName = file.path.replace('\\', '/').replace(dirPath, "")

                if (cacheFileName.startsWith("Maps/")) return@forEach

                val data = file.readBytes()

                if (cacheFileName.startsWith("Models/") && file.extension == "dat") {
                    println("packed data custom model \"$cacheFileName\"")
                    val modelId = file.nameWithoutExtension.toInt()
                    cacheTo.remove(1, modelId)
                    cacheTo.put(1, modelId, data)
                } else {
                    println("packed data \"$cacheFileName\"")
                    cacheTo.put(0, 9, cacheFileName, data)
                }
            }

        cacheTo.index(1).update()
        index.update()
    }

}