package ca.andrej_zeravatech.attackmapper.core

import java.text.Normalizer

/**
 * BERT uncased tokenizer (BasicTokenizer + WordPiece), matching the HuggingFace
 * BertTokenizer used by all-MiniLM-L6-v2: do_lower_case=true, strip accents,
 * tokenize_chinese_chars=true, max_seq_length=256 (incl. [CLS]/[SEP]).
 */
class WordPieceTokenizer(vocabLines: List<String>, private val maxSeqLength: Int = 256) {
    private val vocab: Map<String, Int> = HashMap<String, Int>(vocabLines.size * 2).also { m ->
        vocabLines.forEachIndexed { i, tok -> if (tok.isNotEmpty() || i == 0) m.putIfAbsent(tok, i) }
    }
    private val unkId = id("[UNK]")
    val clsId = id("[CLS]")
    val sepId = id("[SEP]")

    private fun id(t: String) = vocab[t] ?: error("vocab is missing $t")

    /** Token ids with [CLS] ... [SEP], truncated like sentence-transformers. */
    fun encode(text: String): LongArray {
        val pieces = ArrayList<Int>()
        for (word in basicTokenize(text)) pieces.addAll(wordPiece(word))
        val body = if (pieces.size > maxSeqLength - 2) pieces.subList(0, maxSeqLength - 2) else pieces
        val out = LongArray(body.size + 2)
        out[0] = clsId.toLong()
        body.forEachIndexed { i, p -> out[i + 1] = p.toLong() }
        out[out.size - 1] = sepId.toLong()
        return out
    }

    internal fun basicTokenize(text: String): List<String> {
        val cleaned = StringBuilder()
        forEachCodePoint(text) { cp ->
            when {
                cp == 0 || cp == 0xFFFD || isControl(cp) -> Unit
                isWhitespace(cp) -> cleaned.append(' ')
                isChinese(cp) -> cleaned.append(' ').appendCodePoint(cp).append(' ')
                else -> cleaned.appendCodePoint(cp)
            }
        }
        val out = ArrayList<String>()
        for (raw in cleaned.split(' ')) {
            if (raw.isEmpty()) continue
            val token = stripAccents(raw.lowercase())
            val cur = StringBuilder()
            forEachCodePoint(token) { cp ->
                if (isPunctuation(cp)) {
                    if (cur.isNotEmpty()) { out.add(cur.toString()); cur.setLength(0) }
                    out.add(String(Character.toChars(cp)))
                } else {
                    cur.appendCodePoint(cp)
                }
            }
            if (cur.isNotEmpty()) out.add(cur.toString())
        }
        return out
    }

    private fun wordPiece(word: String): List<Int> {
        if (word.codePointCount(0, word.length) > 100) return listOf(unkId)
        val ids = ArrayList<Int>()
        var start = 0
        while (start < word.length) {
            var end = word.length
            var found: Int? = null
            while (start < end) {
                var sub = word.substring(start, end)
                if (start > 0) sub = "##$sub"
                val hit = vocab[sub]
                if (hit != null) { found = hit; break }
                end = word.offsetByCodePoints(end, -1)
            }
            if (found == null) return listOf(unkId)
            ids.add(found)
            start = end
        }
        return ids
    }

    private companion object {
        inline fun forEachCodePoint(s: String, f: (Int) -> Unit) {
            var i = 0
            while (i < s.length) {
                val cp = s.codePointAt(i)
                f(cp)
                i += Character.charCount(cp)
            }
        }

        fun stripAccents(s: String): String {
            val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
            val sb = StringBuilder(nfd.length)
            forEachCodePoint(nfd) { cp ->
                if (Character.getType(cp) != Character.NON_SPACING_MARK.toInt()) sb.appendCodePoint(cp)
            }
            return sb.toString()
        }

        fun isWhitespace(cp: Int): Boolean =
            cp == ' '.code || cp == '\t'.code || cp == '\n'.code || cp == '\r'.code ||
                Character.getType(cp) == Character.SPACE_SEPARATOR.toInt()

        fun isControl(cp: Int): Boolean {
            if (cp == '\t'.code || cp == '\n'.code || cp == '\r'.code) return false
            return when (Character.getType(cp)) {
                Character.CONTROL.toInt(), Character.FORMAT.toInt(), Character.SURROGATE.toInt(),
                Character.PRIVATE_USE.toInt(), Character.UNASSIGNED.toInt() -> true
                else -> false
            }
        }

        fun isPunctuation(cp: Int): Boolean {
            if ((cp in 33..47) || (cp in 58..64) || (cp in 91..96) || (cp in 123..126)) return true
            return when (Character.getType(cp)) {
                Character.CONNECTOR_PUNCTUATION.toInt(), Character.DASH_PUNCTUATION.toInt(),
                Character.START_PUNCTUATION.toInt(), Character.END_PUNCTUATION.toInt(),
                Character.INITIAL_QUOTE_PUNCTUATION.toInt(), Character.FINAL_QUOTE_PUNCTUATION.toInt(),
                Character.OTHER_PUNCTUATION.toInt() -> true
                else -> false
            }
        }

        fun isChinese(cp: Int): Boolean =
            (cp in 0x4E00..0x9FFF) || (cp in 0x3400..0x4DBF) || (cp in 0x20000..0x2A6DF) ||
                (cp in 0x2A700..0x2B73F) || (cp in 0x2B740..0x2B81F) || (cp in 0x2B820..0x2CEAF) ||
                (cp in 0xF900..0xFAFF) || (cp in 0x2F800..0x2FA1F)
    }
}
