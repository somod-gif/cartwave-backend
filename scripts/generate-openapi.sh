#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_JSON="${ROOT_DIR}/docs/openapi.json"
OUTPUT_YAML="${ROOT_DIR}/docs/openapi.yaml"
LOG_FILE="${ROOT_DIR}/target/openapi-generation.log"

cd "${ROOT_DIR}"

./mvnw -q spring-boot:run -Dspring-boot.run.profiles=local >"${LOG_FILE}" 2>&1 &
APP_PID=$!
cleanup() {
  kill "${APP_PID}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

for _ in {1..60}; do
  if curl -sf "http://localhost:8080/api-docs" > "${OUTPUT_JSON}"; then
    break
  fi
  sleep 2
done

if [ ! -s "${OUTPUT_JSON}" ]; then
  echo "Failed to export OpenAPI JSON. Check ${LOG_FILE}" >&2
  exit 1
fi

if command -v yq >/dev/null 2>&1; then
  yq -P . "${OUTPUT_JSON}" > "${OUTPUT_YAML}"
else
  python - <<'PY'
import json, pathlib
root = pathlib.Path('.').resolve()
obj = json.loads((root / 'docs' / 'openapi.json').read_text())
try:
    import yaml
except Exception:
    raise SystemExit(0)
(root / 'docs' / 'openapi.yaml').write_text(yaml.safe_dump(obj, sort_keys=False))
PY
fi

echo "Generated ${OUTPUT_JSON} and (if possible) ${OUTPUT_YAML}"
