#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
ENV_FILE="$ROOT_DIR/.env.prod"
REMOTE="${DEPLOY_REMOTE:-origin}"
BRANCH="${DEPLOY_BRANCH:-main}"

log() {
  echo "==> $*"
}

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

assert_clean_worktree() {
  if [[ -n "$(git status --porcelain)" ]]; then
    git status --short
    fail "Working tree is not clean. Commit, stash, or remove local changes before deploying."
  fi
}

checkout_latest_main() {
  local remote_ref="refs/remotes/$REMOTE/$BRANCH"
  local display_ref="$REMOTE/$BRANCH"

  log "Fetching $display_ref..."
  git fetch "$REMOTE" "$BRANCH:$remote_ref"

  if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
    git checkout "$BRANCH"
  else
    git checkout -b "$BRANCH" "$display_ref"
  fi

  assert_clean_worktree

  local local_only_commits
  local_only_commits="$(git rev-list --left-only --count "$BRANCH...$display_ref")"
  if [[ "$local_only_commits" != "0" ]]; then
    fail "Local $BRANCH has $local_only_commits commit(s) not present on $display_ref. Refusing to reset them away."
  fi

  log "Resetting $BRANCH to $display_ref..."
  git reset --hard "$display_ref"
}

require_command git
require_command npm
require_command docker

cd "$ROOT_DIR"

[[ -f "$ENV_FILE" ]] || fail "Missing production env file: $ENV_FILE"

assert_clean_worktree
checkout_latest_main

log "Installing frontend dependencies..."
npm --prefix "$FRONTEND_DIR" ci

log "Building frontend production assets..."
npm --prefix "$FRONTEND_DIR" run build

log "Starting production containers..."
docker compose --env-file "$ENV_FILE" -f "$ROOT_DIR/docker-compose.yml" up -d --build --remove-orphans

log "Deployment complete."
