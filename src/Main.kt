import strategies.*
import strategies.huffman.*
import java.io.*
import kotlin.system.measureTimeMillis


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
    readAndStoreGzDict()
    val frequencyDictGz: Map<String, Long>
    val gzMs = measureTimeMillis {
        frequencyDictGz = loadGzDict()
    }

    ////////////////////////////////////////////////////////////////
    // FDIC1

    val huffmanBuilder = HuffmanEncoderBuilder()
    frequencyDict.keys.forEach { term: String ->
        huffmanBuilder.addString(term)
    }
    val encoder: HuffmanEncoder = huffmanBuilder.build()

    //writeFdicDOS(fdicFile, encoder, frequencyDict)
    writeFdic1(encoder, frequencyDict)

    //val newDict = loadBinDict()
    val fdict1Loaded: Map<String, Long>
    val fdic1Ms = measureTimeMillis {
        fdict1Loaded = readFdic1()
    }

    ////////////////////////////////////////////////////////////////
    // FDIC2
    writeFdic2Kmp(frequencyDict)

    val fdict2Loaded: Map<String, Long>
    val fdic2Ms = measureTimeMillis {
        fdict2Loaded = readFdic2Kmp()
    }

    ////////////////////////////////////////////////////////////////
    // Results

    println("Binary Frequency Dictionary Compression")
    println("---------------------------------------")
    println()
    println("Speed:")
    println("------")
    println(" txt took ${textMs}ms to load")
    println("  gz took ${gzMs}ms to load")
    println("fdic took ${fdic1Ms}ms to load")
    println("fdi2 took ${fdic2Ms}ms to load")

    println()

    if(textMs < fdic1Ms) {
        val speedPercent = 1.0 - (textMs.toDouble() / fdic1Ms)
        println("fdic1 was ${fdic1Ms - textMs}ms (${percent(speedPercent)}) slower")
    } else {
        val speedPercent = 1.0 - (fdic1Ms / textMs.toDouble())
        println("fdic1 was ${textMs - fdic1Ms}ms (${percent(speedPercent)}) faster! ☹")
    }

    if(textMs < fdic2Ms) {
        val speedPercent = 1.0 - (textMs.toDouble() / fdic2Ms)
        println("fdic2 was ${fdic2Ms - textMs}ms (${percent(speedPercent)}) slower")
    } else {
        val speedPercent = 1.0 - (fdic2Ms / textMs.toDouble())
        println("fdic2 was ${textMs - fdic2Ms}ms (${percent(speedPercent)}) faster!")
    }

    println()
    println("Compression:")
    println("------------")

    println(" txt size: ${fdicTextFile.length() / 1024} KB")
    println("  gz size: ${fdicGzFile.length() / 1024} KB")
    println("fdi1 size: ${fdic1File.length() / 1024} KB")
    println("fdi2 size: ${fdic2File.length() / 1024} KB")

    println()
    val percentOfOriginalSize = (fdic1File.length().toDouble() / fdicTextFile.length().toDouble())
    println("FDIC1 was " + percent(percentOfOriginalSize) + " of the original size")
    val reduction1 = 1.0 - percentOfOriginalSize
    println("a reduction of ${percent(reduction1)} (${fdicTextFile.length() - fdic1File.length()} B)")

    println()

    val percentOfOriginalSize2 = (fdic2File.length().toDouble() / fdicTextFile.length().toDouble())
    println("FDIC2 was " + percent(percentOfOriginalSize2) + " of the original size")

    val reduction2 = 1.0 - percentOfOriginalSize2
    println("a reduction of ${percent(reduction2)} (${fdicTextFile.length() - fdic2File.length()} B)")

    if(fdict1Loaded.size != frequencyDict.size) error("Wrong size ${frequencyDict.size} vs ${fdict1Loaded.size}")
    frequencyDict.forEach { (term, frequency) ->
        if(fdict1Loaded[term] != frequency) error("Wrong frequency for ${term}: $frequency vs ${fdict1Loaded[term]}")
    }

    if(fdict2Loaded.size != frequencyDict.size) error("Wrong size ${frequencyDict.size} vs ${fdict2Loaded.size}")
    frequencyDict.forEach { (term, frequency) ->
        if(fdict2Loaded[term] != frequency) error("Wrong frequency for ${term}: $frequency vs ${fdict2Loaded[term]}")
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