package org.jire.swiftfup.packing

interface XteaRepository {

    operator fun get(region: Int): Xtea?

    operator fun set(region: Int, key: Xtea)

}