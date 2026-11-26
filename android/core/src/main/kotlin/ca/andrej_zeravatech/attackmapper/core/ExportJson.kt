package ca.andrej_zeravatech.attackmapper.core

/** Same JSON shape as the "Export Analysis" button in app.py. No external JSON library. */
object ExportJson {
    fun export(conversation: Conversation, report: RiskReport = Detectors.totalRisk(conversation)): String {
        val sb = StringBuilder()
        sb.append("{\n  \"total_risk\": ").append(num(report.total)).append(",\n")
        sb.append("  \"components\": {")
        report.components.entries.forEachIndexed { i, (k, v) ->
            if (i > 0) sb.append(",")
            sb.append("\n    ").append(str(k)).append(": ").append(num(v))
        }
        sb.append("\n  },\n  \"turns\": [")
        conversation.turns.forEachIndexed { i, t ->
            if (i > 0) sb.append(",")
            sb.append("\n    {\"id\": ").append(t.id)
                .append(", \"role\": ").append(str(t.role.label))
                .append(", \"content\": ").append(str(t.content))
                .append(", \"intent\": ").append(str(t.intent))
                .append(", \"risk\": ").append(num(t.riskScore))
                .append(", \"entities\": [").append(t.entities.joinToString(", ") { str(it) }).append("]}")
        }
        sb.append("\n  ]\n}\n")
        return sb.toString()
    }

    private fun num(d: Double): String = if (d.isFinite()) d.toString() else "null"

    fun str(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append(String.format("\\u%04x", c.code)) else sb.append(c)
            }
        }
        return sb.append('"').toString()
    }
}
