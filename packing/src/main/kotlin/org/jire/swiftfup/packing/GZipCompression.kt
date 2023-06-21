package org.jire.swiftfup.packing

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.GZIPOutputStream

object GZipCompression {

    fun compress(array: ByteArray, compressionLevel: Int = Deflater.BEST_COMPRESSION): ByteArray {
        ByteArrayOutputStream().use { bout ->
            object : GZIPOutputStream(bout) {
                init {
                    def.setLevel(compressionLevel)
                }
            }.use { os ->
                os.write(array)
                os.finish()
            }
            return bout.toByteArray()
        }
    }

}