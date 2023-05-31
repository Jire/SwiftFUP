package org.jire.swiftfup.packing

import com.google.gson.Gson
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.nio.file.Path
import kotlin.io.path.bufferedReader

object DefaultXteaRepository : XteaRepository {

    val map: Int2ObjectMap<Xtea> = Int2ObjectOpenHashMap()

    @JvmStatic
    @JvmOverloads
    fun load(
        path: Path = Path.of("xteas.json"),
        gson: Gson = Gson()
    ) {
        path.bufferedReader().use { reader ->
            val xteas = gson.fromJson(reader, Array<Xtea?>::class.java)
            for (xtea in xteas) {
                xtea ?: continue
                set(xtea.mapsquare, xtea)
            }
        }
    }

    override operator fun get(region: Int): Xtea? = map.get(region)

    override fun set(region: Int, key: Xtea) {
        map[region] = key
    }

}