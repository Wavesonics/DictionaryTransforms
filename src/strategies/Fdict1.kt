package strategies

import utils.decodeVariableLengthLong
import strategies.huffman.HuffmanEncoder
import utils.writeVariableLength
import java.io.*

val fdic1File = File("en-80k.fdic")

fun writeFdic1(encoder: HuffmanEncoder, frequencyDict: Map<String, Long>) {
    //println("------------------------- WRITE FDIC")
    fdic1File.delete()
    BufferedOutputStream(FileOutputStream(fdic1File)).use { outputStream ->
        val huffmanTable = encoder.exportCharToCodeMap()
        val huffmanTableSize = huffmanTable.size
        //println("Table size write: $huffmanTableSize B")
        outputStream.writeVariableLength(huffmanTableSize.toLong())
        // Write the huffman table
        outputStream.write(huffmanTable)

        // Write each term's frequency and encoded data
        frequencyDict.entries.forEach { (term: String, frequency: Long) ->
            outputStream.writeVariableLength(frequency)

            // Encode the term and write its size and content
            // Encode the term with a \n
            val encoded = encoder.encode(term)

            // Write the encoded data itself
            outputStream.write(encoded)
        }
    }
}

fun readFdic1(): Map<String, Long> {
    //println("------------------------- READ FDIC")
    val frequencyDict = mutableMapOf<String, Long>()
    val buffer = CharArray(64)
    BufferedInputStream(FileInputStream(fdic1File)).use { inputStream ->
        // Read the size of the huffmanTable (4 bytes)
        val huffmanTableSize = decodeVariableLengthLong(inputStream).toInt()

        //println("Table size read: $huffmanTableSize B")
        // Read the huffmanTable
        val huffmanTable = ByteArray(huffmanTableSize)
        if (inputStream.read(huffmanTable) != huffmanTableSize) {
            throw EOFException("Unexpected end of file while reading Huffman table")
        }
        val decoder = HuffmanEncoder(huffmanTable)

        // Read the frequency dictionary
        while (inputStream.available() > 0) {
            // Read the frequency
            val frequency = decodeVariableLengthLong(inputStream)
            val term = decoder.decode(inputStream, buffer)
            frequencyDict[term] = frequency
        }

        return frequencyDict
    }
}

//fun loadBinDict(): Map<String, Long> {
//    val fdicTextFile = File("en-80k.fdic")
//    val frequencyDict = mutableMapOf<String, Long>()
//    val ms = measureTimeMillis {
//        DataInputStream(FileInputStream(fdicTextFile)).use { inputStream ->
//            val tableLength = inputStream.readInt()
//            println("tableLength: $tableLength")
//            val huffmanTable = inputStream.readNBytes(tableLength)
//            val encoder = HuffmanEncoder(huffmanTable)
//
//            val frequency = inputStream.readLong()
//            val termLen = inputStream.readByte().toInt()
//            val termEncoded = inputStream.readNBytes(termLen)
//            val term = encoder.decode(termEncoded)
//            frequencyDict[term] = frequency
//        }
//    }
//    println("bin took ${ms}ms to load")
//
//    return frequencyDict
//}
//
//fun writeFdicDOS(fdicFile: File, encoder: HuffmanEncoder, frequencyDict: Map<String, Long>) {
//    fdicFile.delete()
//    DataOutputStream(FileOutputStream(fdicFile)).use { outputStream ->
//        val huffmanTable = encoder.exportCharToCodeMap()
//        outputStream.writeInt(huffmanTable.size)
//        outputStream.write(huffmanTable)
//
//        frequencyDict.entries.forEach { (term: String, frequency: Long) ->
//            outputStream.writeLong(frequency)
//            val encoded = encoder.encode(term)
//
//            println("Term OG size: $term (${term.toByteArray().size})B encoded: ${encoded.size}")
//
//            outputStream.writeByte(encoded.size)
//            outputStream.write(encoded)
//        }
//    }
//}