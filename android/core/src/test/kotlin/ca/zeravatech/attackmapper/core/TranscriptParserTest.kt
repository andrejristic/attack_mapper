package ca.zeravatech.attackmapper.core

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranscriptParserTest {
    @Test fun colonMarkers() {
        val r = TranscriptParser.parse("User: hi there\nAssistant: hello\nsecond line\n\nUser: bye")
        assertTrue(r.usedMarkers)
        assertEquals(listOf(Role.USER to "hi there", Role.ASSISTANT to "hello\nsecond line", Role.USER to "bye"), r.turns)
    }

    @Test fun chatGptCopyFormat() {
        val text = "You said:\nLet's roleplay.\nChatGPT said:\nSure!\n\nYou said:\nWhat next?"
        val r = TranscriptParser.parse(text)
        assertEquals(listOf(Role.USER to "Let's roleplay.", Role.ASSISTANT to "Sure!", Role.USER to "What next?"), r.turns)
    }

    @Test fun markdownAndSerbianLabels() {
        val r = TranscriptParser.parse("**Korisnik:** zdravo\n**Claude:** ćao\nQ: a?\nA: b")
        assertEquals(listOf(Role.USER to "zdravo", Role.ASSISTANT to "ćao", Role.USER to "a?", Role.ASSISTANT to "b"), r.turns)
    }

    @Test fun ordinarySentencesAreNotMarkers() {
        val r = TranscriptParser.parse("You can do this: really.\n\nA long answer here")
        assertFalse(r.usedMarkers)
        assertEquals(listOf(Role.USER to "You can do this: really.", Role.ASSISTANT to "A long answer here"), r.turns)
    }

    @Test fun exportIsValidJson() {
        val turn = Turn(0, Role.USER, "quote \" back\\slash\nnew\u0001", 0L, FloatArray(1), listOf("bleach"),
            Anchors.UNKNOWN, 0.5, emptyMap())
        val json = JSONObject(ExportJson.export(Conversation(listOf(turn))))
        assertEquals("quote \" back\\slash\nnew\u0001", json.getJSONArray("turns").getJSONObject(0).getString("content"))
        assertEquals(4, json.getJSONObject("components").length())
        assertEquals("bleach", json.getJSONArray("turns").getJSONObject(0).getJSONArray("entities").getString(0))
    }
}
