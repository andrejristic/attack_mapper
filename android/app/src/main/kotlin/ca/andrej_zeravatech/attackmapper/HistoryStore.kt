package ca.andrej_zeravatech.attackmapper

import ca.andrej_zeravatech.attackmapper.core.Role
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Saved analyses: only the turns are stored; opening one re-runs the analysis on the device. */
class HistoryStore(filesDir: File) {
    data class Entry(val id: Long, val title: String, val createdMillis: Long, val turns: List<Pair<Role, String>>)

    private val dir = File(filesDir, "history").apply { mkdirs() }

    fun list(): List<Entry> =
        (dir.listFiles { f -> f.name.endsWith(".json") } ?: emptyArray())
            .mapNotNull { runCatching { read(it) }.getOrNull() }
            .sortedByDescending { it.createdMillis }

    fun save(entry: Entry) {
        val turns = JSONArray()
        entry.turns.forEach { (r, c) -> turns.put(JSONObject().put("role", r.label).put("content", c)) }
        val json = JSONObject()
            .put("id", entry.id)
            .put("title", entry.title)
            .put("created", entry.createdMillis)
            .put("turns", turns)
        val tmp = File(dir, "${entry.id}.json.tmp")
        tmp.writeText(json.toString())
        tmp.renameTo(File(dir, "${entry.id}.json"))
    }

    fun delete(id: Long) {
        File(dir, "$id.json").delete()
    }

    private fun read(f: File): Entry {
        val o = JSONObject(f.readText())
        val a = o.getJSONArray("turns")
        val turns = (0 until a.length()).mapNotNull {
            val t = a.getJSONObject(it)
            Role.parse(t.getString("role"))?.let { r -> r to t.getString("content") }
        }
        return Entry(o.getLong("id"), o.optString("title"), o.getLong("created"), turns)
    }
}
