# Deployment

Run the production deployment on the server:

```bash
scripts/deploy.sh
```

The script:

1. Fails if tracked or untracked local work could be overwritten.
2. Fetches `origin/main`.
3. Checks out local `main`, then refuses to continue if local `main` has commits
   that are not on `origin/main`.
4. Resets local `main` to `origin/main`.
5. Runs `npm ci` and `npm run build` in `frontend/`.
6. Runs:

```bash
docker compose --env-file .env.prod -f docker-compose.yml up -d --build --remove-orphans
```

`up -d --build` rebuilds changed images and recreates changed containers without
removing the named volumes. Use `docker compose down` only when you
intentionally want to stop and remove the current compose containers; do not use
`down -v` in production unless you intend to delete the database volume.

## Requirements 

- `.env.prod` exists in the repo root.
- Frontend production variables in `.env.prod` are correct before building,
  because Vite embeds them into `frontend/dist`.
- TLS certificates are present under `certs/` for Nginx.
- Git is available as `git`.
- Node/npm are available as `npm`.
- Docker Compose is available as `docker compose`.

The target remote and branch default to `origin` and `main`. Override them with:

```bash
DEPLOY_REMOTE=origin DEPLOY_BRANCH=main scripts/deploy.sh
```
