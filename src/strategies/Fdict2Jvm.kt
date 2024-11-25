package strategies

import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import utils.*

val fdic2File = File("en-80k.fdic2")

fun writeFdic2(frequencyDict: Map<String, Long>) {
    //println("------------------------- WRITE FDIC")
    fdic2File.delete()

    BufferedOutputStream(GZIPOutputStream(FileOutputStream(fdic2File))).use { outputStream ->
        // Write each term's frequency and encoded data
        frequencyDict.entries.forEach { (term: String, frequency: Long) ->
            outputStream.writeVariableLength(frequency)
            outputStream.writeString(term)
        }
    }
}

fun readFdic2(): Map<String, Long> {
    //println("------------------------- READ FDIC")
    val frequencyDict = mutableMapOf<String, Long>()
    val buffer = CharArray(64)
    BufferedInputStream(GZIPInputStream(FileInputStream(fdic2File))).use { inputStream ->
        // Read the frequency dictionary
        while (inputStream.available() > 0) {
            // Read the frequency
            val frequency = decodeVariableLengthLong(inputStream)
            val term = inputStream.readString(buffer)
            frequencyDict[term] = frequency
        }

        return frequencyDict
    }
}