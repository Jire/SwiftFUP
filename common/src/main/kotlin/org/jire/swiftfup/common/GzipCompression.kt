package org.jire.swiftfup.common

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.GZIPOutputStream

object GzipCompression {

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