package ca.zeravatech.attackmapper.core

import kotlin.math.sqrt

/** Text -> sentence embedding (all-MiniLM-L6-v2 in the app; a fake in unit tests). */
fun interface Embedder {
    fun embed(text: String): FloatArray
}

/** util.cos_sim for two vectors. */
fun cosine(a: FloatArray, b: FloatArray): Double {
    require(a.size == b.size) { "dimension mismatch ${a.size} vs ${b.size}" }
    var dot = 0.0
    var na = 0.0
    var nb = 0.0
    for (i in a.indices) {
        dot += a[i].toDouble() * b[i]
        na += a[i].toDouble() * a[i]
        nb += b[i].toDouble() * b[i]
    }
    val den = sqrt(na) * sqrt(nb)
    return if (den == 0.0) 0.0 else dot / den
}

/**
 * The analysis pipeline from app.py: classify_intent, score_sensitivity, extract_entities,
 * process_turn (incl. similarity edges). Anchor embeddings are computed once, up front.
 */
class Analyzer(
    private val embedder: Embedder,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val intentAnchors: Map<String, List<FloatArray>> =
        Anchors.INTENT_ANCHORS.mapValuesTo(LinkedHashMap()) { (_, phrases) -> phrases.map(embedder::embed) }
    private val sensitiveAnchors: Map<String, List<FloatArray>> =
        Anchors.SENSITIVE_TOPICS.mapValuesTo(LinkedHashMap()) { (_, words) -> words.map(embedder::embed) }

    fun classifyIntent(embedding: FloatArray): String {
        var best = Anchors.UNKNOWN
        var maxScore = -1.0
        for ((intent, anchors) in intentAnchors) {
            val score = anchors.maxOf { cosine(embedding, it) }
            if (score > maxScore) {
                maxScore = score
                best = intent
            }
        }
        return if (maxScore < Anchors.INTENT_MIN_SCORE) Anchors.UNKNOWN else best
    }

    /** Returns (max risk, per-topic score). */
    fun scoreSensitivity(embedding: FloatArray): Pair<Double, Map<String, Double>> {
        var maxRisk = 0.0
        val details = LinkedHashMap<String, Double>()
        for ((topic, anchors) in sensitiveAnchors) {
            val score = anchors.maxOf { cosine(embedding, it) }
            details[topic] = score
            if (score > maxRisk) maxRisk = score
        }
        return maxRisk to details
    }

    /** process_turn(): analyse [content] and append it to [conversation]. */
    fun addTurn(conversation: Conversation, role: Role, content: String): Conversation {
        val id = conversation.turns.size
        val emb = embedder.embed(content)
        val intent = classifyIntent(emb)
        val entities = Entities.extract(content)
        val (sens, details) = scoreSensitivity(emb)
        val turn = Turn(id, role, content, clock(), emb, entities, intent, sens, details)
        val newEdges = conversation.turns.mapNotNull { prev ->
            val sim = cosine(emb, prev.embedding)
            if (sim > Anchors.SIMILARITY_EDGE_MIN) SimilarityEdge(prev.id, id, sim) else null
        }
        return Conversation(conversation.turns + turn, conversation.similarityEdges + newEdges)
    }

    fun analyze(turns: List<Pair<Role, String>>): Conversation =
        turns.fold(Conversation()) { conv, (role, text) -> addTurn(conv, role, text) }
}
