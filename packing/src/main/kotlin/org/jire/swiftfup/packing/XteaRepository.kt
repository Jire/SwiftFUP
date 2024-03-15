package org.jire.swiftfup.packing

import com.google.gson.Gson
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import java.nio.file.Path

interface XteaRepository {

    fun load(
        path: Path = Path.of("data", "osrs", "cache218", "xteas.json"),
        gson: Gson = Gson()
    ): Int2ObjectMap<Xtea>

}