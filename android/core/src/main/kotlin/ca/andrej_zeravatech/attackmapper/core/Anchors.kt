package ca.andrej_zeravatech.attackmapper.core

/**
 * Anchor phrases and keyword lists, copied verbatim from app.py
 * (INTENT_ANCHORS, SENSITIVE_TOPICS, chemical precursor lists).
 * Map iteration order matches the Python dict order, which matters for ties.
 */
object Anchors {
    const val ROLE_ASSIGNMENT = "role_assignment"
    const val HYPOTHETICAL_SCENARIO = "hypothetical_scenario"
    const val DIRECT_REQUEST = "direct_request"
    const val REFUSAL_RESPONSE = "refusal_response"
    const val COMPLIANCE_RESPONSE = "compliance_response"
    const val UNKNOWN = "unknown"

    val INTENT_ANCHORS: Map<String, List<String>> = linkedMapOf(
        ROLE_ASSIGNMENT to listOf(
            "act as a chemistry teacher", "you are a security expert",
            "pretend to be a villain", "let's roleplay"
        ),
        HYPOTHETICAL_SCENARIO to listOf(
            "imagine a world", "what if someone wanted to",
            "write a story about", "hypothetically speaking"
        ),
        DIRECT_REQUEST to listOf(
            "tell me how to", "write code for", "give me the recipe", "explain the process"
        ),
        REFUSAL_RESPONSE to listOf(
            "I cannot fulfill this request", "I'm sorry but I can't",
            "As an AI language model", "I am unable to assist"
        ),
        COMPLIANCE_RESPONSE to listOf(
            "Here is the code", "Sure, I can help with that", "The steps are as follows",
            "To synthesize this", "To mix these ingredients"
        ),
    )

    val SENSITIVE_TOPICS: Map<String, List<String>> = linkedMapOf(
        "bioweapon" to listOf("anthrax", "ricin", "pathogen", "virus", "biological agent"),
        "chemical_weapon" to listOf(
            "sarin", "mustard gas", "nerve agent", "chlorine gas", "precursor", "ammonia", "bleach"
        ),
        "cyber_exploit" to listOf(
            "sql injection", "buffer overflow", "rce", "exploit", "payload", "shellcode", "hack"
        ),
        "personal_data" to listOf("credit card", "social security", "passport number", "private key"),
    )

    /** extract_entities() keyword fallback list. */
    val ENTITY_KEYWORDS = listOf(
        "ammonia", "bleach", "fertilizer", "diesel", "pipe", "pressure cooker", "sarin", "mustard gas"
    )

    /** detect_obfuscation_chain() precursor set. */
    val CHEMICAL_PRECURSORS = setOf("ammonia", "bleach", "fertilizer", "diesel", "pipe", "pressure cooker")

    /** classify_intent(): below this best score the intent is "unknown". */
    const val INTENT_MIN_SCORE = 0.25

    /** process_turn(): similarity edge threshold. */
    const val SIMILARITY_EDGE_MIN = 0.6
}
