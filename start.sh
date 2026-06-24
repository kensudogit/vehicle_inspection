#!/bin/sh
set -e

cd /app/backend
echo "[start] Launching Spring Boot..."
java -jar app.jar 2>&1 | tee /tmp/backend.log &
BACKEND_PID=$!

echo "[start] Waiting for backend /api/health ..."
READY=0
i=0
while [ "$i" -lt 120 ]; do
  if curl -sf http://127.0.0.1:8080/api/health >/dev/null 2>&1; then
    READY=1
    echo "[start] Backend is ready"
    break
  fi
  i=$((i + 1))
  sleep 2
done

if [ "$READY" -eq 0 ]; then
  echo "[start] Backend failed to become healthy. Last log lines:"
  tail -n 40 /tmp/backend.log 2>/dev/null || true
  exit 1
fi

cd /app/frontend
echo "[start] Launching Next.js on port ${PORT:-3000}..."
exec npm start -- -p "${PORT:-3000}" -H 0.0.0.0
