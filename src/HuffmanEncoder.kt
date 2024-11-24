import java.io.InputStream
import java.util.PriorityQueue

class HuffmanEncoder {
    internal val charToCodeMap: MutableMap<Char, BitSequence> = mutableMapOf()
    private var root: Node? = null

    constructor(charFrequency: Map<Char, Int>) {
        val charFreqMap = charFrequency.toMutableMap()
        if (charFreqMap.containsKey(STREAM_DELIMITER).not()) {
            charFreqMap[STREAM_DELIMITER] = charFreqMap.values.max() / 2
        }

        val priorityQueue = PriorityQueue(compareBy<Node> { it.frequency })
        charFreqMap.forEach { (char, frequency) ->
            priorityQueue.add(Node(char, frequency))
        }

        while (priorityQueue.size > 1) {
            val left = priorityQueue.poll()
            val right = priorityQueue.poll()
            val parent = Node(null, left.frequency + right.frequency, left, right)
            priorityQueue.add(parent)
        }

        root = priorityQueue.poll()
        buildCharToCodeMap(root, "")
    }

    constructor(charFrequencyByteArray: ByteArray) {
        importCharToCodeMap(charFrequencyByteArray)
        root = reconstructTreeFromCodes()
    }

    fun encode(input: String): ByteArray {
        val encodedBits = BitSequence()

        // Debug: Print character frequencies in input
        //val actualFreqs = input.groupBy { it }.mapValues { it.value.size }
        //println("Actual character frequencies in input: $actualFreqs")

        // Debug: Print Huffman codes
        //println("Huffman codes:")
//        charToCodeMap.forEach { (char, bits) ->
//            val bitsString = buildString {
//                for (i in 0 until bits.length) {
//                    append(if (bits.getBit(i)) '1' else '0')
//                }
//            }
//            //println("'$char': $bitsString (${bits.length} bits)")
//        }

        // Ensure input ends with delimiter if not already present
        val inputWithDelimiter = if (input.endsWith(STREAM_DELIMITER)) input else input + STREAM_DELIMITER

        // Calculate total bits needed (for debug output only)
        var totalBits = 0
        inputWithDelimiter.forEach { char ->
            val code = charToCodeMap[char] ?: throw IllegalArgumentException("Character '$char' is not in the Huffman tree!")
            totalBits += code.length
        }

        //println("Input size in bits: ${input.length * 8}")
        //println("Encoded size in bits: $totalBits")

        // Encode the actual data
        inputWithDelimiter.forEach { char ->
            val code = charToCodeMap[char] ?: throw IllegalArgumentException("Character '$char' is not in the Huffman tree!")
            for (i in 0 until code.length) {
                encodedBits.append(code.getBit(i))
            }
        }

        return encodedBits.toByteArray()
    }

    fun decode(encoded: ByteArray): String {
        if (root == null) throw IllegalStateException("Huffman tree not initialized")
        val decoded = decodeFromBits(encoded)
        // Remove the delimiter if it exists at the end
        return if (decoded.endsWith(STREAM_DELIMITER)) decoded.dropLast(1) else decoded
    }

    fun decode(inputStream: InputStream): String {
        if (root == null) throw IllegalStateException("Huffman tree not initialized")

        val result = StringBuilder()
        var currentNode = root
        var currentByte = inputStream.read()
        var bitPositionInByte = 0

        while (currentByte != -1) {
            // Process each bit in the current byte
            while (bitPositionInByte < 8) {
                val currentBit = (currentByte and (1 shl (7 - bitPositionInByte))) != 0

                currentNode = if (currentBit) currentNode?.right else currentNode?.left
                    ?: throw IllegalStateException("Invalid Huffman encoding")

                currentNode?.char?.let { char ->
                    result.append(char)
                    if (char == STREAM_DELIMITER) {
                        return result.dropLast(1).toString() // Remove delimiter and return
                    }
                    currentNode = root
                }

                bitPositionInByte++
            }

            // Read next byte and reset bit position
            currentByte = inputStream.read()
            bitPositionInByte = 0
        }

        throw IllegalStateException("Input stream ended before finding delimiter")
    }

    private fun decodeFromBits(encodedBytes: ByteArray): String {
        val result = StringBuilder()
        var currentNode = root
        var bitIndex = 0
        val totalBits = encodedBytes.size * 8

        while (bitIndex < totalBits) {
            val byteIndex = bitIndex / 8
            val currentBit = (encodedBytes[byteIndex].toInt() and (1 shl (7 - (bitIndex % 8)))) != 0

            currentNode = if (currentBit) currentNode?.right else currentNode?.left
                ?: throw IllegalStateException("Invalid Huffman encoding")

            currentNode?.char?.let { char ->
                result.append(char)
                if (char == STREAM_DELIMITER) {
                    return result.toString()
                }
                currentNode = root
            }

            bitIndex++
        }

        throw IllegalStateException("Encoded data did not contain delimiter character")
    }

    private fun buildCharToCodeMap(node: Node?, path: String) {
        if (node == null) return

        node.char?.let { char ->
            charToCodeMap[char] = BitSequence.fromPath(path)
        }

        buildCharToCodeMap(node.left, path + "0")
        buildCharToCodeMap(node.right, path + "1")
    }

    private fun reconstructTreeFromCodes(): Node {
        val root = Node(null, 0)
        charToCodeMap.forEach { (char, bits) ->
            var current = root
            for (i in 0 until bits.length) {
                val bit = bits.getBit(i)
                if (bit) {
                    if (current.right == null) {
                        current.right = Node(null, 0)
                    }
                    current = current.right!!
                } else {
                    if (current.left == null) {
                        current.left = Node(null, 0)
                    }
                    current = current.left!!
                }
            }
            current.char = char
        }
        return root
    }

    fun exportCharToCodeMap(): ByteArray {
        // Convert BitSequence to string representation for serialization
        val serializedMap = charToCodeMap.entries.joinToString(separator = ";") { (char, bits) ->
            val bitsString = buildString {
                for (i in 0 until bits.length) {
                    append(if (bits.getBit(i)) '1' else '0')
                }
            }
            //println("Exporting char '$char' with bits: $bits (as string: $bitsString)")
            "$char:$bitsString"
        }
        return serializedMap.toByteArray(Charsets.UTF_8)
    }

    fun importCharToCodeMap(byteArray: ByteArray) {
        val serializedMap = byteArray.toString(Charsets.UTF_8)
        //println("Importing serialized map: $serializedMap")
        charToCodeMap.clear()
        serializedMap.split(";").forEach { entry ->
            val (char, code) = entry.split(":")
            val bits = BitSequence.fromPath(code)
            //println("Importing char '${char[0]}' with code $code, resulting bits: $bits")
            charToCodeMap[char[0]] = bits
        }
        root = reconstructTreeFromCodes()
    }

    private data class Node(
        var char: Char?,
        val frequency: Int,
        var left: Node? = null,
        var right: Node? = null
    )

    companion object {
        const val STREAM_DELIMITER: Char = '\n'
    }
}