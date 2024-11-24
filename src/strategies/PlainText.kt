package strategies

import java.io.File

val fdicTextFile = File("en-80k.txt")

fun loadTxtDict(): Map<String, Long> {
    val frequencyDict: Map<String, Long> = fdicTextFile.readLines()
        .map { it.split(" ") }
        .associate { it[0] to it[1].toLong() }

    return frequencyDict
}
