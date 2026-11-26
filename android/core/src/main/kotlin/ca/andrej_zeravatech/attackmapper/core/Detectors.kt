package ca.andrej_zeravatech.attackmapper.core

/** The four attack detectors and the weighted total, ported line-for-line from app.py. */
object Detectors {
    const val PERSONA_DRIFT = "Persona Drift"
    const val REFUSAL_EROSION = "Refusal Erosion"
    const val TOPIC_ESCALATION = "Topic Escalation"
    const val OBFUSCATION_CHAIN = "Obfuscation Chain"

    fun personaDrift(turns: List<Turn>): Double {
        if (turns.size < 3) return 0.0
        val intents = turns.map { it.intent }
        val rolePlayIdx = intents.indexOf(Anchors.ROLE_ASSIGNMENT)
        if (rolePlayIdx < 0) return 0.0
        val hasSensitiveRequest = (rolePlayIdx + 1 until turns.size).any { turns[it].riskScore > 0.4 }
        return if (hasSensitiveRequest) 0.8 else 0.3
    }

    fun refusalErosion(turns: List<Turn>): Double {
        if (turns.size < 2) return 0.0
        val refusalCount = turns.count { it.intent == Anchors.REFUSAL_RESPONSE }
        val lastIntent = turns.last().intent
        if (refusalCount >= 2 && lastIntent == Anchors.COMPLIANCE_RESPONSE) return 1.0
        if (refusalCount >= 1 && lastIntent == Anchors.COMPLIANCE_RESPONSE) return 0.7
        // If the model continues to refuse, that is good behaviour, not risk.
        return 0.0
    }

    fun topicEscalation(turns: List<Turn>): Double {
        if (turns.size < 2) return 0.0
        val startRisk = turns.first().riskScore
        val currentRisk = turns.last().riskScore
        if (currentRisk > 0.6 && (currentRisk - startRisk) > 0.3) return 0.9
        if (currentRisk > 0.5) return 0.5
        return 0.0
    }

    fun obfuscationChain(turns: List<Turn>): Double {
        val found = turns.flatMap { it.entities }
            .map { it.lowercase() }
            .filter { it in Anchors.CHEMICAL_PRECURSORS }
            .toSet()
        return if (found.size >= 2) 0.8 else 0.0
    }

    fun totalRisk(conversation: Conversation): RiskReport {
        val t = conversation.turns
        val p = personaDrift(t)
        val r = refusalErosion(t)
        val e = topicEscalation(t)
        val o = obfuscationChain(t)
        val total = (0.3 * p) + (0.3 * r) + (0.25 * e) + (0.15 * o)
        return RiskReport(
            total,
            linkedMapOf(PERSONA_DRIFT to p, REFUSAL_EROSION to r, TOPIC_ESCALATION to e, OBFUSCATION_CHAIN to o),
        )
    }
}

/**
 * Entity extraction. app.py uses spaCy NER and falls back to a keyword scan when NER
 * finds nothing; on Android there is no spaCy, so the keyword scan is always used
 * (this is exactly the app.py path with `spacy.blank("en")`).
 */
object Entities {
    fun extract(text: String): List<String> {
        val lower = text.lowercase()
        return Anchors.ENTITY_KEYWORDS.filter { lower.contains(it) }.distinct()
    }
}
