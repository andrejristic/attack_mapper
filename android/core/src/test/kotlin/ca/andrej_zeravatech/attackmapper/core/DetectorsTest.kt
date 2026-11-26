package ca.andrej_zeravatech.attackmapper.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DetectorsTest {
    private fun t(id: Int, intent: String, risk: Double, vararg entities: String, role: Role = Role.USER) =
        Turn(id, role, "x", 0L, FloatArray(1), entities.toList(), intent, risk, emptyMap())

    private fun conv(vararg turns: Turn) = Conversation(turns.toList())

    @Test fun personaDrift() {
        assertEquals(0.0, Detectors.personaDrift(listOf(t(0, Anchors.ROLE_ASSIGNMENT, 0.9), t(1, "x", 0.9))), 0.0)
        val noRole = listOf(t(0, "a", 0.9), t(1, "b", 0.9), t(2, "c", 0.9))
        assertEquals(0.0, Detectors.personaDrift(noRole), 0.0)
        val roleOnly = listOf(t(0, Anchors.ROLE_ASSIGNMENT, 0.9), t(1, "b", 0.4), t(2, "c", 0.1))
        assertEquals(0.3, Detectors.personaDrift(roleOnly), 0.0)   // 0.4 is not > 0.4; earlier turn ignored
        val drift = listOf(t(0, "a", 0.1), t(1, Anchors.ROLE_ASSIGNMENT, 0.1), t(2, "c", 0.41))
        assertEquals(0.8, Detectors.personaDrift(drift), 0.0)
    }

    @Test fun refusalErosion() {
        assertEquals(0.0, Detectors.refusalErosion(listOf(t(0, Anchors.COMPLIANCE_RESPONSE, 0.0))), 0.0)
        val one = listOf(t(0, Anchors.REFUSAL_RESPONSE, 0.0), t(1, Anchors.COMPLIANCE_RESPONSE, 0.0))
        assertEquals(0.7, Detectors.refusalErosion(one), 0.0)
        val two = listOf(t(0, Anchors.REFUSAL_RESPONSE, 0.0), t(1, Anchors.REFUSAL_RESPONSE, 0.0),
            t(2, Anchors.COMPLIANCE_RESPONSE, 0.0))
        assertEquals(1.0, Detectors.refusalErosion(two), 0.0)
        val stillRefusing = listOf(t(0, Anchors.REFUSAL_RESPONSE, 0.0), t(1, Anchors.REFUSAL_RESPONSE, 0.0))
        assertEquals(0.0, Detectors.refusalErosion(stillRefusing), 0.0)
    }

    @Test fun topicEscalation() {
        assertEquals(0.0, Detectors.topicEscalation(listOf(t(0, "a", 0.9))), 0.0)
        assertEquals(0.9, Detectors.topicEscalation(listOf(t(0, "a", 0.2), t(1, "b", 0.61))), 0.0)
        assertEquals(0.5, Detectors.topicEscalation(listOf(t(0, "a", 0.5), t(1, "b", 0.61))), 0.0)
        assertEquals(0.5, Detectors.topicEscalation(listOf(t(0, "a", 0.5), t(1, "b", 0.55))), 0.0)
        assertEquals(0.0, Detectors.topicEscalation(listOf(t(0, "a", 0.1), t(1, "b", 0.5))), 0.0)
    }

    @Test fun obfuscationChain() {
        assertEquals(0.0, Detectors.obfuscationChain(listOf(t(0, "a", 0.0, "ammonia", "Ammonia"))), 0.0)
        assertEquals(0.0, Detectors.obfuscationChain(listOf(t(0, "a", 0.0, "sarin", "mustard gas"))), 0.0)
        assertEquals(0.8, Detectors.obfuscationChain(listOf(t(0, "a", 0.0, "ammonia"), t(1, "b", 0.0, "Bleach"))), 0.0)
    }

    @Test fun totalWeightsAndLevels() {
        val c = conv(
            t(0, Anchors.ROLE_ASSIGNMENT, 0.1, "ammonia"), t(1, Anchors.REFUSAL_RESPONSE, 0.2),
            t(2, Anchors.REFUSAL_RESPONSE, 0.5, "bleach"), t(3, Anchors.COMPLIANCE_RESPONSE, 0.65),
        )
        val r = Detectors.totalRisk(c)
        assertEquals(listOf(Detectors.PERSONA_DRIFT, Detectors.REFUSAL_EROSION, Detectors.TOPIC_ESCALATION,
            Detectors.OBFUSCATION_CHAIN), r.components.keys.toList())
        assertEquals(0.3 * 0.8 + 0.3 * 1.0 + 0.25 * 0.9 + 0.15 * 0.8, r.total, 1e-12)
        assertEquals(AlertLevel.HIGH, r.level)
        assertEquals(AlertLevel.SAFE, RiskReport(0.3, emptyMap()).level)
        assertEquals(AlertLevel.MONITOR, RiskReport(0.31, emptyMap()).level)
        assertEquals(AlertLevel.MONITOR, RiskReport(0.7, emptyMap()).level)
        assertEquals(RiskBand.LOW, RiskBand.of(0.3))
        assertEquals(RiskBand.MEDIUM, RiskBand.of(0.7))
        assertEquals(RiskBand.HIGH, RiskBand.of(0.71))
    }

    @Test fun entities() {
        assertEquals(listOf("ammonia", "bleach"), Entities.extract("Mixing Ammonia and BLEACH"))
        assertEquals(listOf("pipe", "pressure cooker"), Entities.extract("a pressure cooker and a pipe"))
        assertEquals(emptyList<String>(), Entities.extract("hello"))
    }
}
