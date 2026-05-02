#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
ENV_FILE="$ROOT_DIR/.env.dev"
DB_START_WAIT_SECONDS="${DB_START_WAIT_SECONDS:-2}"
BACKEND_PORT=8080
FRONTEND_PORT=5173

backend_pid=""
frontend_pid=""
cleanup_started=0
compose_started=0

port_in_use() {
  local port="$1"

  if command -v ss >/dev/null 2>&1; then
    ss -ltn "( sport = :$port )" | tail -n +2 | grep -q .
    return
  fi

  if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"$port" -sTCP:LISTEN -t >/dev/null 2>&1
    return
  fi

  (echo >"/dev/tcp/127.0.0.1/$port") >/dev/null 2>&1
}

assert_port_available() {
  local name="$1"
  local port="$2"

  if port_in_use "$port"; then
    echo "$name port $port is already in use." >&2
    echo "Stop the process using it, then run this script again." >&2
    echo "Helpful checks:" >&2
    echo "  ss -ltnp 'sport = :$port'" >&2
    echo "  lsof -iTCP:$port -sTCP:LISTEN" >&2
    exit 1
  fi
}

stop_process_group() {
  local pid="$1"

  if [[ -z "$pid" ]] || ! kill -0 "$pid" 2>/dev/null; then
    return
  fi

  kill -TERM "-$pid" 2>/dev/null || true
}

cleanup() {
  if [[ "$cleanup_started" -eq 1 ]]; then
    return
  fi
  cleanup_started=1
  trap - EXIT INT TERM

  echo
  echo "Stopping dev services..."

  stop_process_group "$frontend_pid"
  stop_process_group "$backend_pid"

  if [[ "$compose_started" -eq 1 ]]; then
    docker compose --env-file "$ENV_FILE" -f "$ROOT_DIR/docker-compose.yml" down
  fi
}

trap cleanup EXIT INT TERM

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing dev env file: $ENV_FILE" >&2
  exit 1
fi

if ! command -v setsid >/dev/null 2>&1; then
  echo "Missing required command: setsid" >&2
  exit 1
fi

assert_port_available "Backend" "$BACKEND_PORT"
assert_port_available "Frontend" "$FRONTEND_PORT"

echo "Starting Postgres db service..."
docker compose --env-file "$ENV_FILE" -f "$ROOT_DIR/docker-compose.yml" up -d db
compose_started=1

echo "Waiting ${DB_START_WAIT_SECONDS}s for Postgres to accept connections..."
sleep "$DB_START_WAIT_SECONDS"

echo "TODO: In the future, need to also start up the map tiling service here as well"

echo "Starting Spring Boot API on http://localhost:${BACKEND_PORT} ..."
setsid bash -c 'cd "$1" && exec ./mvnw spring-boot:run' _ "$BACKEND_DIR" &
backend_pid=$!

echo "Starting Vite frontend on http://localhost:${FRONTEND_PORT} ..."
setsid bash -c 'cd "$1" && exec npm run dev -- --port "$2" --strictPort' _ "$FRONTEND_DIR" "$FRONTEND_PORT" &
frontend_pid=$!

echo
echo "Dev app is starting:"
echo "  Frontend: http://localhost:${FRONTEND_PORT}"
echo "  Backend:  http://localhost:${BACKEND_PORT}"
echo
echo "Press Ctrl-C to stop everything."

set +e
wait -n "$backend_pid" "$frontend_pid"
exit_code=$?
set -e

exit "$exit_code"
