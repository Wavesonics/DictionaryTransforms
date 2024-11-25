package strategies

import okio.FileSystem
import okio.GzipSink
import okio.GzipSource
import okio.Path.Companion.toPath
import okio.buffer
import utils.readString
import utils.readVariableLong
import utils.writeString
import utils.writeVariableLong

val fdic2Path = "en-80k.fdic2".toPath()

fun writeFdic2Kmp(frequencyDict: Map<String, Long>) {
    // Delete existing file if it exists
    FileSystem.SYSTEM.delete(fdic2Path, mustExist = false)

    GzipSink(FileSystem.SYSTEM.sink(fdic2Path)).buffer().use { byteStream ->
        frequencyDict.forEach { (term, frequency) ->
            byteStream.writeVariableLong(frequency)
            byteStream.writeString(term)
        }
    }
}

fun readFdic2Kmp(): Map<String, Long> {
    val frequencyDict = mutableMapOf<String, Long>()

    // Read compressed file
    GzipSource(FileSystem.SYSTEM.source(fdic2Path)).buffer().use { buffer ->
        // Read dictionary entries
        while (!buffer.exhausted()) {
            val frequency = buffer.readVariableLong()
            val term = buffer.readString()
            frequencyDict[term] = frequency
        }
    }

    return frequencyDict
}
