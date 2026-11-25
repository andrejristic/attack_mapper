package ca.zeravatech.attackmapper.core

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.LongBuffer
import kotlin.math.max
import kotlin.math.sqrt

/**
 * all-MiniLM-L6-v2 on ONNX Runtime: tokenizer -> transformer -> mean pooling -> L2 normalize,
 * the same pipeline sentence-transformers runs (modules.json: Transformer, Pooling, Normalize).
 * Works with both onnxruntime (JVM, tests) and onnxruntime-android (app).
 */
class MiniLmEmbedder(modelBytes: ByteArray, vocabLines: List<String>) : Embedder, AutoCloseable {
    val tokenizer = WordPieceTokenizer(vocabLines)
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession = env.createSession(modelBytes, OrtSession.SessionOptions())
    private val inputNames: Set<String> = session.inputNames

    override fun embed(text: String): FloatArray {
        val ids = tokenizer.encode(text)
        val n = ids.size
        val shape = longArrayOf(1, n.toLong())
        val tensors = LinkedHashMap<String, OnnxTensor>()
        try {
            tensors["input_ids"] = OnnxTensor.createTensor(env, LongBuffer.wrap(ids), shape)
            if ("attention_mask" in inputNames) {
                tensors["attention_mask"] = OnnxTensor.createTensor(env, LongBuffer.wrap(LongArray(n) { 1L }), shape)
            }
            if ("token_type_ids" in inputNames) {
                tensors["token_type_ids"] = OnnxTensor.createTensor(env, LongBuffer.wrap(LongArray(n)), shape)
            }
            session.run(tensors).use { result ->
                @Suppress("UNCHECKED_CAST")
                val hidden = (result.get(0).value as Array<Array<FloatArray>>)[0]
                val dim = hidden[0].size
                val pooled = FloatArray(dim)
                for (tok in hidden) for (d in 0 until dim) pooled[d] += tok[d]
                for (d in 0 until dim) pooled[d] /= hidden.size.toFloat()
                var norm = 0.0
                for (v in pooled) norm += v.toDouble() * v
                val inv = 1.0 / max(sqrt(norm), 1e-12)
                return FloatArray(dim) { (pooled[it] * inv).toFloat() }
            }
        } finally {
            tensors.values.forEach { it.close() }
        }
    }

    override fun close() {
        session.close()
    }

    companion object {
        const val MODEL_ASSET = "minilm/model_qint8_arm64.onnx"
        const val VOCAB_ASSET = "minilm/vocab.txt"
    }
}
