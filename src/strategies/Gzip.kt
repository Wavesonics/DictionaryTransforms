package strategies

import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import korlibs.io.lang.UTF8
import korlibs.io.lang.toString
import java.io.File

val fdicGzFile = File("en-80k.gz")

fun loadGzDict(): Map<String, Long> {
    val frequencyDict: Map<String, Long> = fdicGzFile.readBytes().uncompress(GZIP).toString(charset = UTF8).trim().lines()
        .map { it.split(" ") }
        .associate { it[0] to it[1].toLong() }

    return frequencyDict
}

fun readAndStoreGzDict() {
    fdicGzFile.writeBytes(fdicTextFile.readText().toByteArray().compress(GZIP))
}