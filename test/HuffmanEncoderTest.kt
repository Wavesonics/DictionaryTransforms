import junit.framework.TestCase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class HuffmanEncoderTests {

    @Test
    fun testEncodeDecodeConsistency() {
        val charFrequency = mapOf(
            'a' to 5,
            'b' to 9,
            'c' to 12,
            'd' to 13,
            'e' to 16,
            'f' to 45
        )

        val encoder = HuffmanEncoder(charFrequency)
        val input = "abcdef"

        // Encode the input string
        val encoded = encoder.encode(input)
        println("Encoded: ${encoded.toHexString()}")

        // Decode the encoded output
        val decoded = encoder.decode(encoded)
        println("Decoded: $decoded")

        // Assert that the original input is equal to the decoded output
        assertEquals("The decoded output should match the original input: $decoded", input, decoded)
    }

    @Test
    fun testEncodeEmptyString() {
        val charFrequency = mapOf(
            'a' to 1,
            'b' to 1
        )

        val encoder = HuffmanEncoder(charFrequency)
        val input = ""

        // Encode the empty input string
        val encoded = encoder.encode(input)
        println("Encoded empty string: ${encoded.toHexString()}")

        // Decode the encoded output
        val decoded = encoder.decode(encoded)
        println("Decoded empty string: $decoded")

        // Assert that the original input is equal to the decoded output
        assertEquals("The decoded output for an empty string should match the original input", input, decoded)
    }

    @Test
    fun testSingleCharacterRepeated() {
        val charFrequency = mapOf(
            'a' to 10,
            'b' to 5
        )

        val encoder = HuffmanEncoder(charFrequency)
        val input = "aaaaaa"

        // Encode the input string
        val encoded = encoder.encode(input)
        println("Encoded repeated 'a': ${encoded.toHexString()}")

        // Decode the encoded output
        val decoded = encoder.decode(encoded)
        println("Decoded repeated 'a': $decoded")

        // Assert that the original input is equal to the decoded output
        assertEquals("The decoded output should match the original input", input, decoded)
    }

    @Test
    fun testComplexString() {
        val charFrequency = mapOf(
            'a' to 5,
            'b' to 9,
            'c' to 12,
            'd' to 13,
            'e' to 16,
            'f' to 45
        )

        val encoder = HuffmanEncoder(charFrequency)
        val input = "abcdeffedcba"

        // Encode the input string
        val encoded = encoder.encode(input)
        println("Encoded complex string: ${encoded.toHexString()}")

        // Decode the encoded output
        val decoded = encoder.decode(encoded)
        println("Decoded complex string: $decoded")

        // Assert that the original input is equal to the decoded output
        assertEquals("The decoded output should match the original input", input, decoded)
    }


    @Test
    fun testExportImportCharToCodeMap() {
        val charFrequency = mapOf(
            'a' to 5,
            'b' to 9,
            'c' to 12,
            'd' to 13,
            'e' to 16,
            'f' to 45
        )
        val encoder = HuffmanEncoder(charFrequency)

        // Export the character-to-code map to a ByteArray
        val exportedMap = encoder.exportCharToCodeMap()

        // Create a new encoder and import the character-to-code map
        val newEncoder = HuffmanEncoder(charFrequency)
        newEncoder.importCharToCodeMap(exportedMap)

        // Assert that the character-to-code maps are equal
        assertEquals("The imported character-to-code map should match the original", encoder.charToCodeMap, newEncoder.charToCodeMap)
    }

    @Test
    fun testExportCharToCodeMap() {
        val charFrequency = mapOf(
            'a' to 10,
            'b' to 15,
            '\n' to 1  // Add delimiter to frequency map
        )
        val encoder = HuffmanEncoder(charFrequency)

        // Export the character-to-code map
        val exportedMap = encoder.exportCharToCodeMap()

        // Create the expected map string using the same format as exportCharToCodeMap
        val expectedMapString = encoder.charToCodeMap.entries.joinToString(separator = ";") { (char, bits) ->
            val bitsString = buildString {
                for (i in 0 until bits.length) {
                    append(if (bits.getBit(i)) '1' else '0')
                }
            }
            "$char:$bitsString"
        }
        val expectedMapBytes = expectedMapString.toByteArray(Charsets.UTF_8)

        // Debug output
        println("Expected map string: $expectedMapString")
        println("Actual exported map: ${exportedMap.toString(Charsets.UTF_8)}")

        // Assert that the exported map matches the expected serialized format
        assertEquals(
            "The exported map should match the expected serialized format",
            expectedMapBytes.toList(),
            exportedMap.toList()
        )
    }

    @Test
    fun testImportCharToCodeMap() {
        val charFrequency = mapOf(
            'x' to 1,
            'y' to 3,
            'z' to 5,
            '\n' to 1  // Add delimiter to frequency map
        )
        val encoder = HuffmanEncoder(charFrequency)

        // Print original map
        println("Original map:")
        encoder.charToCodeMap.forEach { (char, bits) ->
            println("'$char': $bits")
        }

        // Export the character-to-code map
        val exportedMap = encoder.exportCharToCodeMap()
        println("Exported map as string: ${exportedMap.toString(Charsets.UTF_8)}")

        // Create a new encoder and import the exported map
        val newEncoder = HuffmanEncoder(charFrequency)
        newEncoder.importCharToCodeMap(exportedMap)

        // Print both maps for comparison
        println("\nComparing maps:")
        println("Original map:")
        encoder.charToCodeMap.forEach { (char, bits) ->
            println("'$char': $bits")
        }
        println("\nImported map:")
        newEncoder.charToCodeMap.forEach { (char, bits) ->
            println("'$char': $bits")
        }

        // Compare maps entry by entry to find any differences
        encoder.charToCodeMap.forEach { (char, bits) ->
            val newBits = newEncoder.charToCodeMap[char]
            if (newBits == null) {
                println("Character '$char' missing from imported map!")
            } else if (bits != newBits) {
                println("Mismatch for char '$char':")
                println("Original bits: $bits")
                println("Imported bits: $newBits")
                // Compare bit by bit
                for (i in 0 until maxOf(bits.length, newBits.length)) {
                    if (i >= bits.length || i >= newBits.length || bits.getBit(i) != newBits.getBit(i)) {
                        println("Difference at bit $i: ${if (i < bits.length) bits.getBit(i) else "none"} vs ${if (i < newBits.length) newBits.getBit(i) else "none"}")
                    }
                }
            }
        }

        assertEquals("The imported map should match the original map", encoder.charToCodeMap, newEncoder.charToCodeMap)
    }

    @Test
    fun testDecodeInputStream() {
        val charFrequency = mapOf(
            'a' to 5,
            'b' to 9,
            'c' to 12,
            'd' to 13,
            'e' to 16,
            'f' to 45,
            '\n' to 1  // Ensure delimiter is in frequency map with a reasonable frequency
        )
        val encoder = HuffmanEncoder(charFrequency)
        val inputString = "abcdeffedcba"  // Remove explicit delimiter, encoder will add it
        val encodedBytes = encoder.encode(inputString)

        println("Original input: $inputString")
        println("Encoded bytes: ${encodedBytes.toHexString()}")

        val inputStream = ByteArrayInputStream(encodedBytes)
        val decodedString = encoder.decode(inputStream)

        println("Decoded string: $decodedString")

        assertEquals(
            "The decoded string should match the original input string",
            inputString,
            decodedString
        )
    }

    @Test
    fun testEncodedStringIsSmaller() {
        val charFrequency = mapOf(
            'a' to 45,
            'b' to 13,
            'c' to 12,
            'd' to 16,
            'e' to 9,
            'f' to 5
        )
        val encoder = HuffmanEncoder(charFrequency)
        val inputString = "aaaabbbcccddeeeffff"

        // Encode the input string
        val encodedBytes = encoder.encode(inputString)

        // Assert that the encoded size is smaller than the original size
        val originalSize = inputString.toByteArray().size * 8 // original size in bits
        val encodedSize = encodedBytes.size * 8 // encoded size in bits

        assertTrue("The encoded size should be smaller than the original size", encodedSize < originalSize)
    }
}

// Extension function to convert ByteArray to a hex string for easier debugging
fun ByteArray.toHexString(): String {
    return joinToString(separator = " ") { byte -> "%02x".format(byte) }
}
