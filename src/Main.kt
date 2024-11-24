import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import korlibs.io.lang.UTF8
import korlibs.io.lang.toString
import java.io.*
import kotlin.system.measureTimeMillis

fun loadTxtDict(): Map<String, Long> {
    val fdicTextFile = File("en-80k.txt")

    val frequencyDict: Map<String, Long> = fdicTextFile.readLines()
        .map { it.split(" ") }
        .associate { it[0] to it[1].toLong() }

    return frequencyDict
}

fun loadGzDict(): Map<String, Long> {
    val fdicTextFile = File("en-80k.gz")

    val frequencyDict: Map<String, Long> = fdicTextFile.readBytes().uncompress(GZIP).toString(charset = UTF8).trim().lines()
        .map { it.split(" ") }
        .associate { it[0] to it[1].toLong() }

    return frequencyDict
}

fun loadBinDict(): Map<String, Long> {
    val fdicTextFile = File("en-80k.fdic")
    val frequencyDict = mutableMapOf<String, Long>()
    val ms = measureTimeMillis {
        DataInputStream(FileInputStream(fdicTextFile)).use { inputStream ->
            val tableLength = inputStream.readInt()
            println("tableLength: $tableLength")
            val huffmanTable = inputStream.readNBytes(tableLength)
            val encoder = HuffmanEncoder(huffmanTable)

            val frequency = inputStream.readLong()
            val termLen = inputStream.readByte().toInt()
            val termEncoded = inputStream.readNBytes(termLen)
            val term = encoder.decode(termEncoded)
            frequencyDict[term] = frequency
        }
    }
    println("bin took ${ms}ms to load")

    return frequencyDict
}

fun writeFdicDOS(fdicFile: File, encoder: HuffmanEncoder, frequencyDict: Map<String, Long>) {
    fdicFile.delete()
    DataOutputStream(FileOutputStream(fdicFile)).use { outputStream ->
        val huffmanTable = encoder.exportCharToCodeMap()
        outputStream.writeInt(huffmanTable.size)
        outputStream.write(huffmanTable)

        frequencyDict.entries.forEach { (term: String, frequency: Long) ->
            outputStream.writeLong(frequency)
            val encoded = encoder.encode(term)

            println("Term OG size: $term (${term.toByteArray().size})B encoded: ${encoded.size}")

            outputStream.writeByte(encoded.size)
            outputStream.write(encoded)
        }
    }
}

fun writeFdicRAW(fdicFile: File, encoder: HuffmanEncoder, frequencyDict: Map<String, Long>) {
    //println("------------------------- WRITE FDIC")
    fdicFile.delete()
    BufferedOutputStream(FileOutputStream(fdicFile)).use { outputStream ->
        val huffmanTable = encoder.exportCharToCodeMap()
        val huffmanTableSize = huffmanTable.size
        //println("Table size write: $huffmanTableSize B")
        outputStream.writeVariableLength(huffmanTableSize.toLong())

        // Write the huffman table (adjust based on its data structure)
        outputStream.write(huffmanTable)

        // Write each term's frequency and encoded data
        frequencyDict.entries.forEach { (term: String, frequency: Long) ->
            // Write frequency as 8 bytes
            outputStream.writeVariableLength(frequency)

            // Encode the term and write its size and content
            // Encode the term with a \n
            val encoded = encoder.encode(term)

            // Write the encoded data itself
            outputStream.write(encoded)
        }
    }
}

private fun BufferedOutputStream.writeVariableLength(frequency: Long) {
    val encodedFrequency = encodeVariableLengthLong(frequency)
    encodedFrequency.forEach { element ->
        write(element.toInt())
    }
}

fun readFdicRAW(fdicFile: File): Map<String, Long> {
    //println("------------------------- READ FDIC")
    val frequencyDict = mutableMapOf<String, Long>()
    val buffer = CharArray(64)
    BufferedInputStream(FileInputStream(fdicFile)).use { inputStream ->
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

fun main() {
    ////////////////////////////////////////////////////////////////
    // Txt
    // Load the frequency dictionary from the file "en-80.txt".
    val fdicTextFile = File("en-80k.txt")

    val frequencyDict: Map<String, Long>
    val textMs = measureTimeMillis {
        frequencyDict = loadTxtDict()
    }

    ////////////////////////////////////////////////////////////////
    // GZ
    val fdicGzFile = File("en-80k.gz")
    fdicGzFile.writeBytes(fdicTextFile.readText().toByteArray().compress(GZIP))

    val frequencyDictGz: Map<String, Long>
    val gzMs = measureTimeMillis {
        frequencyDictGz = loadGzDict()
    }

    ////////////////////////////////////////////////////////////////
    // FDIC

    val huffmanBuilder = HuffmanEncoderBuilder()
    frequencyDict.keys.forEach { term: String ->
        huffmanBuilder.addString(term)
    }
    val encoder: HuffmanEncoder = huffmanBuilder.build()

    val fdicFile = File("en-80k.fdic")
    //writeFdicDOS(fdicFile, encoder, frequencyDict)
    writeFdicRAW(fdicFile, encoder, frequencyDict)

    //val newDict = loadBinDict()
    val newDict: Map<String, Long>
    val binMs = measureTimeMillis {
        newDict = readFdicRAW(fdicFile)
    }

    println("Binary Frequency Dictionary Compression")
    println("---------------------------------------")
    println()
    println("Speed:")
    println("------")
    println(" txt took ${textMs}ms to load")
    println("  gz took ${gzMs}ms to load")
    println("fdic took ${binMs}ms to load")

    println()

    if(textMs < binMs) {
        val speedPercent = textMs.toDouble() / binMs
        println("fdic was ${binMs - textMs}ms (${percent(speedPercent)}) slower ")
    } else {
        val speedPercent = binMs / textMs.toDouble()
        println("fdic was ${textMs - binMs}ms (${percent(speedPercent)}) faster")
    }

    println()
    println("Compression:")
    println("------------")

    val size = fdicFile.length()
    println(" txt size: ${fdicTextFile.length() / 1024} KB")
    println("  gz size: ${fdicGzFile.length() / 1024} KB")
    println("fdic size: ${size / 1024} KB")
    val percentOfOriginalSize = (size.toDouble() / fdicTextFile.length().toDouble())

    println()
    println("fdic was " + percent(percentOfOriginalSize) + " of the original size")

    val reduction = 1.0 - percentOfOriginalSize
    println("a reduction of ${percent(reduction)} (${fdicTextFile.length() - size} B)")

    if(newDict.size != frequencyDict.size) error("Wrong size ${frequencyDict.size} vs ${newDict.size}")
    frequencyDict.forEach { (term, frequency) ->
        if(newDict[term] != frequency) error("Wrong frequency for ${term}: $frequency vs ${newDict[term]}")
    }
    println()
    println("----")
    println("fdict is valid")
}

fun percent(percent: Double): String {
    return "%.2f%%".format(percent * 100)
}

fun ByteArray.toHexString(): String {
    return joinToString(separator = "") { byte -> "%02x".format(byte) }
}

inline fun <T> measureAndPrintTime(description: String = "", block: () -> T): T {
    val start = System.nanoTime()
    val result = block()
    val end = System.nanoTime()
    println("$description: ${(end - start) / 1_000_000}ms")
    return result
}