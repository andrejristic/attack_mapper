package ca.andrej_zeravatech.attackmapper

import ca.andrej_zeravatech.attackmapper.core.Anchors
import ca.andrej_zeravatech.attackmapper.core.Detectors
import java.util.Locale

enum class Lang(val code: String) {
    SR("sr"), EN("en");

    companion object {
        fun fromCode(code: String?): Lang? = entries.firstOrNull { it.code == code }

        /** Serbian (and closely related locales) -> SR, everything else -> EN. */
        fun systemDefault(): Lang =
            if (Locale.getDefault().language in setOf("sr", "hr", "bs", "sh", "cnr", "me")) SR else EN
    }
}

/** All UI text. The in-app SR/EN switch picks one of the two instances below. */
data class Strings(
    val tabConversation: String,
    val tabMap: String,
    val tabThreats: String,
    val roleUser: String,
    val roleAssistant: String,
    val messageHint: String,
    val addTurn: String,
    val transcript: String,
    val empty: String,
    val loadingModel: String,
    val modelFailed: String,
    val analyzing: String,
    val totalRisk: String,
    val levelHigh: String,
    val levelMonitor: String,
    val levelSafe: String,
    val components: String,
    val flagged: String,
    val noneFlagged: String,
    val turn: String,
    val graph: String,
    val timeline: String,
    val legendLow: String,
    val legendMedium: String,
    val legendHigh: String,
    val legendSimilarity: String,
    val examples: String,
    val exBenign: String,
    val exJailbreak: String,
    val exAttack: String,
    val paste: String,
    val pasteHint: String,
    val next: String,
    val importTitle: String,
    val importNoMarkers: String,
    val importTapRole: String,
    val importNothing: String,
    val analyze: String,
    val cancel: String,
    val close: String,
    val export: String,
    val exportSubject: String,
    val history: String,
    val historyEmpty: String,
    val delete: String,
    val newAnalysis: String,
    val about: String,
    val aboutText: String,
    val language: String,
    val entities: String,
    val menu: String,
    val intents: Map<String, String>,
    val detectors: Map<String, String>,
) {
    fun intent(key: String) = intents[key] ?: key
    fun detector(key: String) = detectors[key] ?: key

    companion object {
        fun of(lang: Lang) = if (lang == Lang.SR) SR else EN

        private const val ABOUT_LICENSES =
            "Attack Mapper — Zerava Technologies Inc. — GPL-3.0-or-later.\n" +
                "Model: sentence-transformers/all-MiniLM-L6-v2 (Apache-2.0), ONNX Runtime (MIT)."

        val SR = Strings(
            tabConversation = "Razgovor",
            tabMap = "Mapa",
            tabThreats = "Pretnje",
            roleUser = "Korisnik",
            roleAssistant = "AI",
            messageHint = "Poruka…",
            addTurn = "Dodaj potez",
            transcript = "Transkript",
            empty = "Dodaj poteze, nalepi razgovor ili učitaj primer iz menija ⋮ da vidiš analizu.",
            loadingModel = "Učitavam model na uređaju…",
            modelFailed = "Model nije učitan",
            analyzing = "Analiziram…",
            totalRisk = "Ukupni rizik",
            levelHigh = "VISOKA UZBUNA: moguć napad",
            levelMonitor = "PRATI: sumnjivi obrasci",
            levelSafe = "BEZBEDNO: normalan razgovor",
            components = "Komponente rizika",
            flagged = "Označeni potezi",
            noneFlagged = "Nema označenih poteza.",
            turn = "Potez",
            graph = "Graf razgovora",
            timeline = "Rizik kroz poteze",
            legendLow = "nizak",
            legendMedium = "srednji",
            legendHigh = "visok",
            legendSimilarity = "sličnost > 0.6",
            examples = "Primeri",
            exBenign = "Primer: benigni razgovor",
            exJailbreak = "Primer: jailbreak (jedan potez)",
            exAttack = "Primer: napad (više poteza)",
            paste = "Nalepi razgovor",
            pasteHint = "Nalepi tekst razgovora, npr.\nUser: …\nAssistant: …",
            next = "Dalje",
            importTitle = "Uvoz razgovora",
            importNoMarkers = "Nisam našao oznake govornika, pa su pasusi raspoređeni naizmenično.",
            importTapRole = "Dodirni ulogu da je promeniš.",
            importNothing = "U tekstu nema poruka.",
            analyze = "Analiziraj",
            cancel = "Otkaži",
            close = "Zatvori",
            export = "Izvezi JSON",
            exportSubject = "Attack Mapper — analiza",
            history = "Istorija",
            historyEmpty = "Još nema sačuvanih analiza.",
            delete = "Obriši",
            newAnalysis = "Nova analiza",
            about = "O aplikaciji",
            aboutText = "Attack Mapper otkriva jailbreak napade i obrasce napada u AI razgovorima " +
                "kroz više poteza: pomeranje persone, eroziju odbijanja, eskalaciju teme i lanac prikrivanja.\n\n" +
                "Privatnost: sve se računa na ovom uređaju. Aplikacija nema dozvolu za internet i ništa ne šalje. " +
                "Istorija se čuva samo lokalno i nije uključena u rezervne kopije.\n\n" + ABOUT_LICENSES,
            language = "Jezik",
            entities = "entiteti",
            menu = "Meni",
            intents = mapOf(
                Anchors.ROLE_ASSIGNMENT to "dodela uloge",
                Anchors.HYPOTHETICAL_SCENARIO to "hipotetički scenario",
                Anchors.DIRECT_REQUEST to "direktan zahtev",
                Anchors.REFUSAL_RESPONSE to "odbijanje",
                Anchors.COMPLIANCE_RESPONSE to "povinovanje",
                Anchors.UNKNOWN to "nepoznato",
            ),
            detectors = mapOf(
                Detectors.PERSONA_DRIFT to "Pomeranje persone",
                Detectors.REFUSAL_EROSION to "Erozija odbijanja",
                Detectors.TOPIC_ESCALATION to "Eskalacija teme",
                Detectors.OBFUSCATION_CHAIN to "Lanac prikrivanja",
            ),
        )

        val EN = Strings(
            tabConversation = "Conversation",
            tabMap = "Map",
            tabThreats = "Threats",
            roleUser = "User",
            roleAssistant = "AI",
            messageHint = "Message…",
            addTurn = "Add turn",
            transcript = "Transcript",
            empty = "Add turns, paste a conversation or load an example from the ⋮ menu to see the analysis.",
            loadingModel = "Loading the on-device model…",
            modelFailed = "Model failed to load",
            analyzing = "Analyzing…",
            totalRisk = "Total risk score",
            levelHigh = "HIGH ALERT: Potential attack detected",
            levelMonitor = "MONITOR: Suspicious patterns",
            levelSafe = "SAFE: Normal conversation",
            components = "Risk components",
            flagged = "Flagged turns",
            noneFlagged = "No flagged turns.",
            turn = "Turn",
            graph = "Conversation graph",
            timeline = "Risk timeline",
            legendLow = "low",
            legendMedium = "medium",
            legendHigh = "high",
            legendSimilarity = "similarity > 0.6",
            examples = "Examples",
            exBenign = "Example: benign conversation",
            exJailbreak = "Example: jailbreak (single-turn)",
            exAttack = "Example: attack (multi-turn)",
            paste = "Paste conversation",
            pasteHint = "Paste the conversation text, e.g.\nUser: …\nAssistant: …",
            next = "Next",
            importTitle = "Import conversation",
            importNoMarkers = "No speaker labels found, so paragraphs alternate between the two roles.",
            importTapRole = "Tap a role to switch it.",
            importNothing = "No messages found in the text.",
            analyze = "Analyze",
            cancel = "Cancel",
            close = "Close",
            export = "Export JSON",
            exportSubject = "Attack Mapper — analysis",
            history = "History",
            historyEmpty = "No saved analyses yet.",
            delete = "Delete",
            newAnalysis = "New analysis",
            about = "About",
            aboutText = "Attack Mapper detects jailbreaks and attack patterns in multi-turn AI conversations: " +
                "persona drift, refusal erosion, topic escalation and obfuscation chains.\n\n" +
                "Privacy: everything is computed on this device. The app has no internet permission and sends " +
                "nothing. History is stored only locally and is excluded from backups.\n\n" + ABOUT_LICENSES,
            language = "Language",
            entities = "entities",
            menu = "Menu",
            intents = mapOf(
                Anchors.ROLE_ASSIGNMENT to "role assignment",
                Anchors.HYPOTHETICAL_SCENARIO to "hypothetical scenario",
                Anchors.DIRECT_REQUEST to "direct request",
                Anchors.REFUSAL_RESPONSE to "refusal",
                Anchors.COMPLIANCE_RESPONSE to "compliance",
                Anchors.UNKNOWN to "unknown",
            ),
            detectors = mapOf(
                Detectors.PERSONA_DRIFT to "Persona drift",
                Detectors.REFUSAL_EROSION to "Refusal erosion",
                Detectors.TOPIC_ESCALATION to "Topic escalation",
                Detectors.OBFUSCATION_CHAIN to "Obfuscation chain",
            ),
        )
    }
}
