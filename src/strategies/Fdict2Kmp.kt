package strategies

import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import utils.*

val fdic2Path = Path("en-80k.fdic2")

fun writeFdic2Kmp(frequencyDict: Map<String, Long>) {
    SystemFileSystem.delete(fdic2Path, mustExist = false)

    // Create a buffer to write to
    val buffer = Buffer()

    // Write to buffer
    frequencyDict.forEach { (term, frequency) ->
        buffer.writeVariableLength(frequency)
        buffer.writeString(term)
    }

    val compressedBuffer = Buffer()
    buffer.readByteArray().compress(GZIP).forEach { byte ->
        compressedBuffer.writeByte(byte)
    }

    // Write to file
    SystemFileSystem.sink(fdic2Path).use { sink ->
        sink.write(compressedBuffer, compressedBuffer.size)
    }
}

fun readFdic2Kmp(): Map<String, Long> {
    val frequencyDict = mutableMapOf<String, Long>()
    val buffer = CharArray(64)

    // Read file into buffer
    //val fieSize = SystemFileSystem.metadataOrNull(fdic2Path)!!.size
    var fileBuffer: ByteArray
    SystemFileSystem.source(fdic2Path).buffered().use { source ->
        fileBuffer = source.readByteArray()
    }

    // Decompress
    val decompressedBuffer = Buffer()
    fileBuffer.uncompress(GZIP).forEach { byte -> decompressedBuffer.writeByte(byte) }

    // Read dictionary entries
    while (!decompressedBuffer.exhausted()) {
        val frequency = decompressedBuffer.decodeVariableLengthLong()
        val term = decompressedBuffer.readString(buffer)
        frequencyDict[term] = frequency
    }

    return frequencyDict
}