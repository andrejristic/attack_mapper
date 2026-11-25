#!/usr/bin/env bash
# Downloads the pinned all-MiniLM-L6-v2 files (Apache-2.0) into the Android assets
# and verifies their SHA-256. Safe to re-run; skips files that already verify.
set -euo pipefail
cd "$(dirname "$0")/.."
REV=1110a243fdf4706b3f48f1d95db1a4f5529b4d41
BASE="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/$REV"
DEST=android/app/src/main/assets/minilm
mkdir -p "$DEST"

fetch() { # <remote path> <local name> <sha256>
  local out="$DEST/$2"
  if [ -f "$out" ] && echo "$3  $out" | sha256sum -c --status; then echo "ok   $2"; return; fi
  curl -fsSL --retry 3 -o "$out.part" "$BASE/$1"
  echo "$3  $out.part" | sha256sum -c --status || { echo "SHA-256 mismatch for $1" >&2; rm -f "$out.part"; exit 1; }
  mv "$out.part" "$out"; echo "got  $2"
}

fetch onnx/model_qint8_arm64.onnx model_qint8_arm64.onnx 4278337fd0ff3c68bfb6291042cad8ab363e1d9fbc43dcb499fe91c871902474
fetch vocab.txt vocab.txt 07eced375cec144d27c900241f3e339478dec958f92fddbc551f295c992038a3
