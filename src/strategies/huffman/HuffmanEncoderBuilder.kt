package strategies.huffman

class HuffmanEncoderBuilder {
    private val charFrequency: MutableMap<Char, Int> = mutableMapOf()

    // Add a string to the frequency map
    fun addString(input: String): HuffmanEncoderBuilder {
        input.forEach { char ->
            charFrequency[char] = charFrequency.getOrDefault(char, 0) + 1
        }
        return this
    }

    fun insert(char: Char, frequency: Int) {
        charFrequency[char] = frequency
    }

    fun numCharacters(): Int = charFrequency.size

    // Build and return a HuffmanEncoder instance
    fun build(): HuffmanEncoder {
        return HuffmanEncoder(charFrequency)
    }
}