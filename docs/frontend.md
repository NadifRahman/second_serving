# Frontend

React, TypeScript, Vite, Mantine, TanStack Query, Zustand, React Router, Zod,
and MapLibre power the browser app.

## Development

Install dependencies and run Vite:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to the Spring Boot backend at
`http://localhost:8080`, as configured in `frontend/vite.config.ts`.

## API Types

The frontend uses generated OpenAPI types from the Spring backend. Start Spring
Boot, then regenerate the schema when DTOs or controller responses change:

```bash
scripts/generate-api-types.sh
```

Generated types live in `frontend/src/api/schema.ts` and should not be edited
manually. Feature API modules import friendly aliases from
`frontend/src/api/types.ts`.

## Configuration

Vite environment variables configure backend and map behavior:

```txt
VITE_API_BASE_URL=
VITE_MAP_STYLE_URL=
VITE_DEFAULT_LATITUDE=43.6532
VITE_DEFAULT_LONGITUDE=-79.3832
VITE_DEFAULT_RADIUS_METERS=50000
VITE_MAX_RADIUS_METERS=100000
```

Vite is configured with `envDir: '..'`, so frontend env variables live in the
repo root beside the backend/container values.

Development uses root `.env.dev`, because `npm run dev` runs
`vite --mode dev`. That file points to OpenFreeMap so the local app has a useful
basemap immediately.

Production uses root `.env.prod`, because `npm run build` runs
`vite build --mode prod`. That file is gitignored and should point to a
same-origin `/map-style/...` path intended to be proxied by Nginx to the
self-hosted tile service. Vite reads this value during `npm run build`, so
changing the production map style URL after deployment requires rebuilding the
frontend bundle or Docker image.

When `VITE_MAP_STYLE_URL` is empty, MapLibre uses a local minimal style so
listing markers can still render before any tile service is configured.

## Structure

```txt
src/app/          global providers, router, query client
src/api/          fetch helper, generated schema, query keys, type aliases
src/config/       routes, env, map defaults
src/features/     auth, listings, reservations, map feature code
src/shared/       reusable components and utilities
src/stores/       Zustand client state stores
```

TanStack Query owns server state. Zustand is reserved for local UI state such as
the current map center/radius and a synchronous mirror of the authenticated user.
Zod schemas live beside the feature forms they validate.
