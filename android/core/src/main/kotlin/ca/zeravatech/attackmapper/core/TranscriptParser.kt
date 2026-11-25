package ca.zeravatech.attackmapper.core

/**
 * Turns a pasted/shared chat transcript into (role, text) turns.
 *
 * Recognised speaker markers at the start of a line (case-insensitive), e.g.
 *   "User: ...", "You said:", "Assistant: ...", "ChatGPT said:", "Claude:", "AI: ..."
 * If no marker is found, blank-line separated paragraphs alternate User / Assistant
 * (the app lets the person flip any role before analysing).
 */
object TranscriptParser {
    data class Result(val turns: List<Pair<Role, String>>, val usedMarkers: Boolean)

    private val USER_LABELS = listOf(
        "you said", "user", "you", "human", "me", "prompt", "question", "q",
        "korisnik", "ja", "pitanje",
    )
    private val ASSISTANT_LABELS = listOf(
        "chatgpt said", "claude said", "gemini said", "copilot said", "assistant", "ai", "chatgpt", "gpt",
        "claude", "gemini", "copilot", "bard", "bot", "model", "llm", "grok", "llama", "mistral",
        "deepseek", "answer", "a", "asistent", "odgovor",
    )

    private val marker: Regex = run {
        val all = (USER_LABELS + ASSISTANT_LABELS).sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }
        // "Label:" with optional text after it, or a line that is only "Label" / "Label said".
        Regex("""^\s*(?:[*_#>\s]*)($all)\s*(?:[*_]*)\s*(?::|：|$)[*_]*\s*(.*)$""", RegexOption.IGNORE_CASE)
    }

    private fun roleOf(label: String): Role {
        val l = label.lowercase()
        return if (l in USER_LABELS) Role.USER else Role.ASSISTANT
    }

    fun parse(text: String): Result {
        val lines = text.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        val turns = ArrayList<Pair<Role, String>>()
        var curRole: Role? = null
        val cur = StringBuilder()
        var markers = 0

        fun flush() {
            val body = cur.toString().trim()
            if (curRole != null && body.isNotEmpty()) turns.add(curRole!! to body)
            cur.setLength(0)
        }

        for (line in lines) {
            val m = marker.matchEntire(line)
            // Single-letter labels ("q", "a") only count when followed by a colon.
            val ok = m != null && (m.groupValues[1].length > 1 || line.contains(':') || line.contains('：'))
            if (ok) {
                flush()
                markers++
                curRole = roleOf(m!!.groupValues[1])
                val rest = m.groupValues[2]
                if (rest.isNotBlank()) cur.append(rest.trim())
            } else if (curRole != null) {
                if (cur.isNotEmpty()) cur.append('\n')
                cur.append(line)
            }
        }
        flush()
        if (markers > 0 && turns.isNotEmpty()) return Result(turns, true)

        // Fallback: alternate paragraphs, starting with the user.
        val paragraphs = text.split(Regex("""\n\s*\n""")).map { it.trim() }.filter { it.isNotEmpty() }
        val alt = paragraphs.mapIndexed { i, p -> (if (i % 2 == 0) Role.USER else Role.ASSISTANT) to p }
        return Result(alt, false)
    }
}
