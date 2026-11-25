package ca.zeravatech.attackmapper

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.zeravatech.attackmapper.core.Analyzer
import ca.zeravatech.attackmapper.core.Conversation
import ca.zeravatech.attackmapper.core.Detectors
import ca.zeravatech.attackmapper.core.ExportJson
import ca.zeravatech.attackmapper.core.MiniLmEmbedder
import ca.zeravatech.attackmapper.core.Role
import ca.zeravatech.attackmapper.core.TranscriptParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface ModelState {
    data object Loading : ModelState
    data object Ready : ModelState
    data class Failed(val message: String) : ModelState
}

/** A shared/pasted conversation waiting for the person to confirm roles. */
data class PendingImport(val turns: List<Pair<Role, String>>, val usedMarkers: Boolean)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val history = HistoryStore(app.filesDir)
    private val lock = Mutex()
    private var analyzer: Analyzer? = null
    private var sessionId: Long? = null

    var modelState by mutableStateOf<ModelState>(ModelState.Loading)
        private set
    var conversation by mutableStateOf(Conversation())
        private set
    var report by mutableStateOf(Detectors.totalRisk(Conversation()))
        private set
    var busy by mutableStateOf(false)
        private set
    var lang by mutableStateOf(Lang.fromCode(prefs.getString("lang", null)) ?: Lang.systemDefault())
        private set
    var pendingImport by mutableStateOf<PendingImport?>(null)
        private set
    var historyEntries by mutableStateOf<List<HistoryStore.Entry>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            try {
                analyzer = withContext(Dispatchers.Default) {
                    val assets = getApplication<Application>().assets
                    val model = assets.open(MiniLmEmbedder.MODEL_ASSET).use { it.readBytes() }
                    val vocab = assets.open(MiniLmEmbedder.VOCAB_ASSET).bufferedReader().use { it.readLines() }
                    Analyzer(MiniLmEmbedder(model, vocab))
                }
                modelState = ModelState.Ready
            } catch (t: Throwable) {
                modelState = ModelState.Failed(t.message ?: t.javaClass.simpleName)
            }
        }
    }

    fun setLanguage(l: Lang) {
        lang = l
        prefs.edit().putString("lang", l.code).apply()
    }

    fun addTurn(role: Role, text: String) {
        val a = analyzer ?: return
        val content = text.trim()
        if (content.isEmpty()) return
        viewModelScope.launch {
            lock.withLock {
                busy = true
                try {
                    val updated = withContext(Dispatchers.Default) { a.addTurn(conversation, role, content) }
                    publish(updated)
                } finally {
                    busy = false
                }
            }
        }
    }

    /** Replace the current analysis with [turns] (example, import or history). */
    fun load(turns: List<Pair<Role, String>>, newSession: Boolean = true, id: Long? = null) {
        val a = analyzer ?: return
        viewModelScope.launch {
            lock.withLock {
                busy = true
                try {
                    val conv = withContext(Dispatchers.Default) { a.analyze(turns) }
                    sessionId = if (newSession) null else id
                    publish(conv)
                } finally {
                    busy = false
                }
            }
        }
    }

    fun clear() {
        sessionId = null
        publish(Conversation(), save = false)
    }

    private fun publish(conv: Conversation, save: Boolean = true) {
        conversation = conv
        report = Detectors.totalRisk(conv)
        if (save && !conv.isEmpty) autosave(conv)
    }

    private fun autosave(conv: Conversation) {
        val id = sessionId ?: System.currentTimeMillis().also { sessionId = it }
        val first = conv.turns.firstOrNull { it.role == Role.USER } ?: conv.turns.first()
        val title = first.content.replace('\n', ' ').take(80)
        val turns = conv.turns.map { it.role to it.content }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { history.save(HistoryStore.Entry(id, title, id, turns)) }
        }
    }

    // ---- import (share / paste / text selection) ----

    fun offerImport(text: String?) {
        if (text.isNullOrBlank()) return
        val parsed = TranscriptParser.parse(text)
        pendingImport = PendingImport(parsed.turns, parsed.usedMarkers)
    }

    fun flipImportRole(index: Int) {
        val p = pendingImport ?: return
        pendingImport = p.copy(turns = p.turns.mapIndexed { i, t ->
            if (i == index) (if (t.first == Role.USER) Role.ASSISTANT else Role.USER) to t.second else t
        })
    }

    fun confirmImport() {
        val p = pendingImport ?: return
        pendingImport = null
        if (p.turns.isNotEmpty()) load(p.turns)
    }

    fun dismissImport() {
        pendingImport = null
    }

    // ---- history ----

    fun refreshHistory() {
        viewModelScope.launch {
            historyEntries = withContext(Dispatchers.IO) { runCatching { history.list() }.getOrDefault(emptyList()) }
        }
    }

    fun openHistory(e: HistoryStore.Entry) = load(e.turns, newSession = false, id = e.id)

    fun deleteHistory(e: HistoryStore.Entry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { history.delete(e.id) }
            if (sessionId == e.id) sessionId = null
            refreshHistory()
        }
    }

    fun exportJson(): String = ExportJson.export(conversation, report)

    val ready: Boolean get() = modelState == ModelState.Ready
}
