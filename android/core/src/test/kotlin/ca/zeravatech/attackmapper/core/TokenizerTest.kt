package ca.zeravatech.attackmapper.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenizerTest {
    private val vocab = listOf("[PAD]", "[UNK]", "[CLS]", "[SEP]", "[MASK]", "hello", ",", "world", "!",
        "un", "##aff", "##able", "cafe", "我", "'", "s", "it")
    private val tok = WordPieceTokenizer(vocab, maxSeqLength = 6)

    @Test fun basicSplitting() {
        assertEquals(listOf("hello", ",", "world", "!", "!"), tok.basicTokenize("  Hello,\tWORLD!!  "))
        assertEquals(listOf("cafe", "naive"), tok.basicTokenize("Café naïve"))
        assertEquals(listOf("it", "'", "s", "我", "爱"), tok.basicTokenize("It's我爱"))
        assertEquals(emptyList<String>(), tok.basicTokenize("\u0000​ �"))
    }

    @Test fun wordPieceAndTruncation() {
        assertArrayEquals(longArrayOf(2, 9, 10, 11, 3), tok.encode("unaffable"))
        assertArrayEquals(longArrayOf(2, 1, 3), tok.encode("xyz"))
        // max 6 incl. [CLS]/[SEP] -> 4 pieces kept
        assertArrayEquals(longArrayOf(2, 5, 6, 7, 8, 3), tok.encode("hello, world! hello"))
        assertArrayEquals(longArrayOf(2, 3), tok.encode(""))
    }
}
