import type { StyleSpecification } from 'maplibre-gl'
import { env } from './env'

/**
 * Minimal style used while no tile service is configured.
 *
 * MapLibre still needs a valid style object. A plain background keeps the map
 * canvas alive and lets our listing markers be evaluated before the self-hosted
 * tile service is plugged in.
 */
export const emptyMapStyle: StyleSpecification = {
  version: 8,
  sources: {},
  layers: [
    {
      id: 'background',
      type: 'background',
      paint: {
        'background-color': '#edf4ea',
      },
    },
  ],
}

/**
 * Map display defaults shared by all map views.
 */
export const mapConfig = {
  defaultCenter: {
    latitude: env.defaultLatitude,
    longitude: env.defaultLongitude,
  },
  defaultZoom: 10,
  detailZoom: 14,
  defaultRadiusMeters: env.defaultRadiusMeters,
  maxRadiusMeters: env.maxRadiusMeters,
  style: env.mapStyleUrl || emptyMapStyle,
} as const
