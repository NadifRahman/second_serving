const numberFromEnv = (value: string | undefined, fallback: number) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

/**
 * Runtime configuration read from Vite environment variables.
 *
 * These defaults let development work without a tile service or custom backend
 * URL. Production can provide explicit values at build time through `VITE_*`
 * variables.
 */
export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '',
  mapStyleUrl: import.meta.env.VITE_MAP_STYLE_URL,
  defaultLatitude: numberFromEnv(import.meta.env.VITE_DEFAULT_LATITUDE, 43.6532),
  defaultLongitude: numberFromEnv(import.meta.env.VITE_DEFAULT_LONGITUDE, -79.3832),
  defaultRadiusMeters: numberFromEnv(
    import.meta.env.VITE_DEFAULT_RADIUS_METERS,
    50_000,
  ),
  maxRadiusMeters: numberFromEnv(import.meta.env.VITE_MAX_RADIUS_METERS, 100_000),
} as const
