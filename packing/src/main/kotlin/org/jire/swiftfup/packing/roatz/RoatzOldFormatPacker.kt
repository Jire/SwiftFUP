package org.jire.swiftfup.packing.roatz

import com.displee.cache.CacheLibrary
import io.netty.buffer.Unpooled
import org.jire.swiftfup.common.GzipCompression
import java.awt.Toolkit
import java.io.File
import javax.swing.ImageIcon

internal object RoatzOldFormatPacker {

    private const val SOUNDS = true
    private const val SPRITES = true
    private const val DATA = true

    fun pack(cachePath: String, cacheTo: CacheLibrary) {
        if (SOUNDS) sounds(cachePath, cacheTo)
        if (SPRITES) sprites(cachePath, cacheTo)
        if (DATA) data(cachePath, cacheTo)
    }

    private const val SOUNDS_INDEX = 3

    private fun sounds(cachePath: String, cacheTo: CacheLibrary) {
        val index = cacheTo.index(SOUNDS_INDEX).apply { cache(); clear() }

        val indexBuf = Unpooled.buffer()
        indexBuf.writeShort(0)

        var amount = 0
        for (soundFile in File("${cachePath}Sounds").listFiles()!!) {
            val id = soundFile.nameWithoutExtension.toInt()
            val data = soundFile.readBytes()

            indexBuf.writeShort(id)
            cacheTo.remove(SOUNDS_INDEX, id)
            cacheTo.put(SOUNDS_INDEX, id, data)
            amount++
        }

        indexBuf.setShort(0, amount)

        val indexBufArray = ByteArray(indexBuf.writerIndex())
        indexBuf.readBytes(indexBufArray)

        val compressed = GzipCompression.compress(indexBufArray)

        cacheTo.put(0, 5, "sounds_index.gz", compressed)

        // don't need this, since it's now gzipped format
        cacheTo.remove(0, 5, "sounds_index")

        index.update()
    }

    private const val CUSTOM_SPRITES_INDEX = 5

    private fun sprites(cachePath: String, cacheTo: CacheLibrary) {
        val index = if (cacheTo.exists(CUSTOM_SPRITES_INDEX)) cacheTo.index(CUSTOM_SPRITES_INDEX).apply { clear() }
        else cacheTo.createIndex().also { cacheTo.reload() }

        val indexBuf = Unpooled.buffer()
        var amount = 0
        indexBuf.writeShort(amount)

        val dirPath = "${cachePath}Sprites/"
        File(dirPath)
            .walkTopDown()
            .forEach { file ->
                if (!file.isFile) return@forEach

                if (".DS_Store" == file.name) {
                    file.delete()
                    return@forEach
                }
                val data = file.readBytes()
                val image = Toolkit.getDefaultToolkit().createImage(data)!!
                val sprite = ImageIcon(image)

                val cacheFileName = file.path.replace('\\', '/').replace(dirPath, "")
                println("\"$cacheFileName\"")
                //cacheTo.put(0, 9, cacheFileName, data)
                val archive = cacheTo.put(CUSTOM_SPRITES_INDEX, cacheFileName, data)

                println("archive ID was ${archive.id} and hashName is ${archive.hashName}")
                indexBuf.writeShort(archive.id)
                indexBuf.writeInt(archive.hashName)

                indexBuf.writeShort(sprite.iconWidth)
                indexBuf.writeShort(sprite.iconHeight)

                amount++
            }

        indexBuf.setShort(0, amount)

        val array = ByteArray(indexBuf.writerIndex())
        indexBuf.readBytes(array)

        val compressedArray = GzipCompression.compress(array)

        cacheTo.put(0, 5, "custom_sprites_index.gz", compressedArray)

        /* don't need these, since it's now gzipped format */
        cacheTo.remove(0, 5, "sprites_index")
        cacheTo.remove(0, 5, "custom_sprites_index")

        cacheTo.index(0).update()

        index.update()
    }

    private fun data(cachePath: String, cacheTo: CacheLibrary) {
        val index = cacheTo.index(0)

        val dirPath = "${cachePath}Data/"
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
                    cacheTo.put(0, 2, cacheFileName, data)
                }
            }

        cacheTo.index(1).update()
        index.update()
    }

}