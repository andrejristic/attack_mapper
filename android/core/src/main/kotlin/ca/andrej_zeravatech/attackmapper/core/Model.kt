package ca.andrej_zeravatech.attackmapper.core

enum class Role(val label: String) {
    USER("User"),
    ASSISTANT("Assistant");

    companion object {
        fun parse(s: String): Role? = when (s.trim().lowercase()) {
            "user" -> USER
            "assistant" -> ASSISTANT
            else -> null
        }
    }
}

/** One analysed turn — mirrors TurnNode in app.py. */
class Turn(
    val id: Int,
    val role: Role,
    val content: String,
    val timestampMillis: Long,
    val embedding: FloatArray,
    val entities: List<String>,
    val intent: String,
    /** Max cosine similarity to any sensitive-topic keyword (score_sensitivity). */
    val riskScore: Double,
    val riskDetails: Map<String, Double>,
)

/** "similarity" edge between two turns (cosine > 0.6). Sequence edges are implicit (i-1 -> i). */
data class SimilarityEdge(val source: Int, val target: Int, val score: Double)

/** Mirrors ConversationGraph in app.py. Immutable snapshots make it safe to share with UI. */
data class Conversation(
    val turns: List<Turn> = emptyList(),
    val similarityEdges: List<SimilarityEdge> = emptyList(),
) {
    val isEmpty: Boolean get() = turns.isEmpty()
}

enum class AlertLevel { SAFE, MONITOR, HIGH }

/** Result of calculate_total_risk(). Component keys keep the Python names and order. */
data class RiskReport(val total: Double, val components: Map<String, Double>) {
    /** Same thresholds as the Streamlit UI: > 0.7 high alert, > 0.3 monitor. */
    val level: AlertLevel
        get() = when {
            total > 0.7 -> AlertLevel.HIGH
            total > 0.3 -> AlertLevel.MONITOR
            else -> AlertLevel.SAFE
        }
}

/** Node colour band used by the graph in app.py (risk > 0.7 red, > 0.3 orange, else green). */
enum class RiskBand { LOW, MEDIUM, HIGH;
    companion object {
        fun of(risk: Double): RiskBand = when {
            risk > 0.7 -> HIGH
            risk > 0.3 -> MEDIUM
            else -> LOW
        }
    }
}

/** "Flagged Turns" threshold from the Streamlit UI. */
const val FLAGGED_TURN_MIN = 0.4
