#!/bin/sh
set -e

cd /app/backend
java -jar app.jar &
BACKEND_PID=$!

cd /app/frontend
npm start -- -p "${PORT:-3000}" -H 0.0.0.0 &
FRONTEND_PID=$!

trap 'kill $BACKEND_PID $FRONTEND_PID 2>/dev/null' TERM INT

wait $FRONTEND_PID
