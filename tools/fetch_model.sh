#!/usr/bin/env bash
# Downloads the pinned all-MiniLM-L6-v2 files (Apache-2.0), verifies their SHA-256 and builds
# the app's model: the original fp32 ONNX with weights stored as float16
# (tools/make_fp16_weights.py; needs python3 with `onnx` and `numpy`).
# Safe to re-run; downloads are cached in tools/.cache.
set -euo pipefail
cd "$(dirname "$0")/.."
REV=1110a243fdf4706b3f48f1d95db1a4f5529b4d41
BASE="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/$REV"
CACHE=tools/.cache
DEST=android/app/src/main/assets/minilm
mkdir -p "$CACHE" "$DEST"

fetch() { # <remote path> <local file> <sha256>
  if [ -f "$2" ] && echo "$3  $2" | sha256sum -c --status; then echo "ok   $2"; return; fi
  curl -fsSL --retry 3 -o "$2.part" "$BASE/$1"
  echo "$3  $2.part" | sha256sum -c --status || { echo "SHA-256 mismatch for $1" >&2; rm -f "$2.part"; exit 1; }
  mv "$2.part" "$2"; echo "got  $2"
}

fetch onnx/model.onnx "$CACHE/model_fp32.onnx" 6fd5d72fe4589f189f8ebc006442dbb529bb7ce38f8082112682524616046452
fetch vocab.txt "$DEST/vocab.txt" 07eced375cec144d27c900241f3e339478dec958f92fddbc551f295c992038a3
rm -f "$DEST/model_qint8_arm64.onnx"
python3 tools/make_fp16_weights.py "$CACHE/model_fp32.onnx" "$DEST/model_fp16w.onnx"
sha256sum "$DEST/model_fp16w.onnx"
