#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SQL_FILE="$ROOT_DIR/scripts/seed-demo-data.sql"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env.dev}"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"

usage() {
  cat <<'EOF'
Usage:
  scripts/seed-demo-data.sh [--local-compose]
  scripts/seed-demo-data.sh --database-url "$DATABASE_URL"

Seeds realistic southern Ontario demo data into the current Second Serving
schema. The script is idempotent: running it again updates the same demo rows.

Default:
  --local-compose
    Uses docker compose, .env.dev, and the db service.

Live/staging:
  --database-url "$DATABASE_URL"
    Runs the SQL with psql against an explicit PostgreSQL connection string.
    This intentionally requires a URL so production seeding is explicit.
EOF
}

mode="local-compose"
database_url=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --local-compose)
      mode="local-compose"
      shift
      ;;
    --database-url)
      mode="database-url"
      database_url="${2:-}"
      [[ -n "$database_url" ]] || {
        echo "Missing value for --database-url" >&2
        exit 1
      }
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

[[ -f "$SQL_FILE" ]] || {
  echo "Missing SQL file: $SQL_FILE" >&2
  exit 1
}

if [[ "$mode" == "database-url" ]]; then
  command -v psql >/dev/null 2>&1 || {
    echo "Missing required command: psql" >&2
    exit 1
  }

  echo "Seeding demo data into explicit database URL..."
  psql "$database_url" --set ON_ERROR_STOP=on --file "$SQL_FILE"
  echo "Demo data seed complete."
  exit 0
fi

[[ -f "$ENV_FILE" ]] || {
  echo "Missing env file: $ENV_FILE" >&2
  exit 1
}

command -v docker >/dev/null 2>&1 || {
  echo "Missing required command: docker" >&2
  exit 1
}

echo "Starting local db service if needed..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d db

echo "Seeding demo data into local compose db..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T db \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" --set ON_ERROR_STOP=on' < "$SQL_FILE"

echo "Demo data seed complete."
