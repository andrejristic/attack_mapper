package ca.zeravatech.attackmapper.core

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import kotlin.math.abs

/**
 * Kotlin port vs the original app.py, using tools/reference/out/reference.json
 * (written by tools/reference/make_reference.py). In CI AM_REQUIRE_PARITY=1 makes
 * missing inputs a failure instead of a skip.
 */
class ParityTest {
    companion object {
        private var ref: JSONObject? = null
        private var embedder: MiniLmEmbedder? = null

        @BeforeClass @JvmStatic fun load() {
            val require = System.getProperty("am.requireParity") == "1"
            val refFile = File(System.getProperty("am.reference") ?: "")
            val assets = File(System.getProperty("am.assets") ?: "")
            val model = File(assets, MiniLmEmbedder.MODEL_ASSET)
            val vocab = File(assets, MiniLmEmbedder.VOCAB_ASSET)
            val missing = listOf(refFile, model, vocab).filterNot { it.isFile }
            if (missing.isNotEmpty()) {
                if (require) fail("parity inputs missing: $missing")
                return
            }
            ref = JSONObject(refFile.readText())
            embedder = MiniLmEmbedder(model.readBytes(), vocab.readLines())
        }
    }

    private fun need(): Pair<JSONObject, MiniLmEmbedder> {
        assumeTrue("reference/model not available", ref != null && embedder != null)
        return ref!! to embedder!!
    }

    @Test fun tokenIdsMatchHuggingFace() {
        val (r, e) = need()
        val toks = r.getJSONArray("tokens")
        for (i in 0 until toks.length()) {
            val o = toks.getJSONObject(i)
            val expected = o.getJSONArray("ids").let { a -> LongArray(a.length()) { a.getLong(it) } }
            val actual = e.tokenizer.encode(o.getString("text"))
            assertArrayEquals("tokens for <${o.getString("text").take(60)}>", expected, actual)
        }
    }

    @Test fun embeddingsMatchPythonOnnx() {
        val (r, e) = need()
        val embs = r.getJSONArray("embeddings_q")
        var worst = 0.0
        for (i in 0 until embs.length()) {
            val o = embs.getJSONObject(i)
            val expected = o.getJSONArray("vec")
            val actual = e.embed(o.getString("text"))
            assertEquals(expected.length(), actual.size)
            for (d in actual.indices) worst = maxOf(worst, abs(actual[d] - expected.getDouble(d)))
        }
        println("embedding max |diff| vs python onnx = $worst")
        assertTrue(worst < 2e-3, "embedding drift $worst")
    }

    @Test fun pipelineMatchesAppPy() {
        val (r, e) = need()
        val analyzer = Analyzer(e) { 0L }
        val pipes = r.getJSONObject("pipelines")
        for (name in pipes.keySet()) {
            val p = pipes.getJSONObject(name)
            val input = p.getJSONArray("turns_in").let { a ->
                (0 until a.length()).map { val t = a.getJSONArray(it); Role.parse(t.getString(0))!! to t.getString(1) }
            }
            val conv = analyzer.analyze(input)
            val report = Detectors.totalRisk(conv)
            for (variant in listOf("q", "fp32")) {
                val exp = p.getJSONObject(variant)
                // q = same model as the app (tight); fp32 = original app.py model (decisions must match,
                // scores may drift a little because of int8 quantization).
                val tol = if (variant == "q") 2e-3 else 5e-2
                val turns = exp.getJSONArray("turns")
                for (i in 0 until turns.length()) {
                    val et = turns.getJSONObject(i)
                    val at = conv.turns[i]
                    assertEquals("$name/$variant turn $i intent", et.getString("intent"), at.intent)
                    assertEquals("$name/$variant turn $i risk", et.getDouble("risk"), at.riskScore, tol)
                    assertEquals("$name/$variant turn $i entities", strings(et.getJSONArray("entities")),
                        at.entities.sorted())
                }
                val comps = exp.getJSONObject("components")
                for ((k, v) in report.components) {
                    assertEquals("$name/$variant $k", comps.getDouble(k), v, 1e-9)
                }
                assertEquals("$name/$variant total", exp.getDouble("total"), report.total, 1e-9)
            }
            // Similarity edges (cosine > 0.6) vs the quantized reference.
            val expEdges = p.getJSONObject("q").getJSONArray("edges").let { a ->
                (0 until a.length()).map { val x = a.getJSONArray(it); x.getInt(0) to x.getInt(1) }.toSet()
            }
            assertEquals("$name edges", expEdges, conv.similarityEdges.map { it.source to it.target }.toSet())
            println("$name: total=${"%.3f".format(report.total)} level=${report.level} " +
                "intents=${conv.turns.map { it.intent }}")
        }
    }

    private fun strings(a: JSONArray) = (0 until a.length()).map { a.getString(it) }.sorted()

    private fun assertTrue(cond: Boolean, msg: String) = org.junit.Assert.assertTrue(msg, cond)
}
