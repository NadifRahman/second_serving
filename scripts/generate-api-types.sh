#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
SCHEMA_URL="${API_SCHEMA_URL:-http://localhost:8080/v3/api-docs}"

if command -v curl >/dev/null 2>&1; then
  if ! curl --fail --silent --output /dev/null "$SCHEMA_URL"; then
    echo "Could not reach the OpenAPI schema at $SCHEMA_URL" >&2
    echo "Start the Spring Boot app in dev mode first, then run this script again." >&2
    echo "For example:" >&2
    echo "  cd backend" >&2
    echo "  ./mvnw spring-boot:run" >&2
    exit 1
  fi
else
  echo "Missing optional command: curl" >&2
  echo "Skipping schema availability check; make sure Spring Boot is running in dev mode." >&2
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "Missing required command: npm" >&2
  echo "Install Node.js/npm, then run this script again." >&2
  exit 1
fi

cd "$FRONTEND_DIR"
npm run api:types
