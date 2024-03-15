package org.jire.swiftfup.packing

import com.google.gson.Gson
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.nio.file.Path
import kotlin.io.path.bufferedReader

object DefaultXteaRepository : XteaRepository {

    override fun load(
        path: Path,
        gson: Gson
    ): Int2ObjectMap<Xtea> {
        val map: Int2ObjectMap<Xtea> = Int2ObjectOpenHashMap()
        val xteas: Array<Xtea?>
        path.bufferedReader().use { reader ->
            xteas = gson.fromJson(reader, Array<Xtea?>::class.java)
        }
        for (xtea in xteas) {
            xtea ?: continue
            map[xtea.mapsquare] = xtea
        }
        return map
    }

}