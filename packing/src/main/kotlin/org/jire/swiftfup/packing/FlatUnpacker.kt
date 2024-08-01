package org.jire.swiftfup.packing

import com.displee.cache.CacheLibrary
import net.runelite.cache.IndexType
import java.io.File


object FlatUnpacker {

    @JvmStatic
    fun main(args: Array<String>) {
        val input = CacheLibrary.create("../server/cache/")
        if (true) {
            input.index(IndexType.CLIENTSCRIPT.number).cache()
            for (archive in input.index(IndexType.CLIENTSCRIPT.number).archives()) {
                File("scripts/${archive.id}")
                    .writeBytes(input.data(IndexType.CLIENTSCRIPT.number, archive.id)!!)
            }
            input.close()
            return
        }

        input.index(3).cache()
        val interIds = intArrayOf(
            5104, 5105, 5107, 5108, 5109, 5110, 5111, 5112, 5113, 5114, 5116/*, 5115, 5116,*/
            //984, 1012
        )
        for (interId in interIds) {
            println("doing $interId...")
            for (file in input.index(3).archive(interId)!!.files) {
                File("interfaces/$interId/").mkdirs()
                File("interfaces/$interId/${file.key}").writeBytes(file.value.data!!)
            }
        }
        input.index(12).cache()
        File("34039.dat").writeBytes(input.data(12, 34039)!!)
        input.index(8).cache()
        for (i in 10147..10154) {
            File("sprites/").mkdirs()
            File("sprites/$i.dat").writeBytes(input.data(8, i)!!)
        }
        input.close()
    }

}
