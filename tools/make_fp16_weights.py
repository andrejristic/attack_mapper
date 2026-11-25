#!/usr/bin/env python3
"""
fp32 ONNX -> same graph with large weights stored as float16 + a Cast back to float32.

All computation stays float32 (ONNX Runtime folds the Casts at load time), only the
stored weights are rounded to float16. This halves the file (~90 MB -> ~45 MB) while
keeping embeddings within ~1e-4 of the original model, unlike int8 dynamic quantization,
which moved app.py's per-turn risk scores by up to ~0.05 and across its 0.3/0.4 thresholds.

  usage: make_fp16_weights.py <model.onnx> <out.onnx>
"""
import sys

import numpy as np
import onnx
from onnx import TensorProto, helper, numpy_helper

MIN_ELEMENTS = 1024  # small tensors (biases, LayerNorm params, shapes) stay float32


def convert(src, dst):
    m = onnx.load(src)
    g = m.graph
    converted, new_inits, casts = set(), [], []
    for init in list(g.initializer):
        if init.data_type != TensorProto.FLOAT:
            continue
        arr = numpy_helper.to_array(init)
        if arr.size < MIN_ELEMENTS:
            continue
        half = numpy_helper.from_array(arr.astype(np.float16), name=init.name + "__fp16")
        new_inits.append(half)
        casts.append(helper.make_node("Cast", [half.name], [init.name], to=TensorProto.FLOAT,
                                      name="cast_" + init.name))
        converted.add(init.name)
        g.initializer.remove(init)
    g.initializer.extend(new_inits)
    # Old IR versions also list initializers as graph inputs; those are now node outputs.
    for inp in [i for i in g.input if i.name in converted]:
        g.input.remove(inp)
    nodes = casts + list(g.node)
    del g.node[:]
    g.node.extend(nodes)
    onnx.checker.check_model(m)
    onnx.save(m, dst)
    print(f"{len(converted)} weight tensors stored as float16 -> {dst}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    convert(sys.argv[1], sys.argv[2])
