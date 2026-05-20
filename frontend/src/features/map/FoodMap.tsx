import { Badge, Button, Group, Paper, Stack, Text } from '@mantine/core'
import { MapPin } from 'lucide-react'
import type { ReactNode } from 'react'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import Map, {
  Layer,
  type MapRef,
  Marker,
  Popup,
  Source,
  type LayerProps,
  type MarkerDragEvent,
  type ViewStateChangeEvent,
} from 'react-map-gl/maplibre'
import type { FeatureCollection, Position } from 'geojson'
import type { FoodListing } from '../../api/types'
import { mapConfig } from '../../config/map'
import { routes } from '../../config/routes'
import { getListingStatusStyle } from '../listings/statusStyles'

type DraggableMarker = {
  latitude: number
  longitude: number
  label: string
  onDragEnd: (latitude: number, longitude: number) => void
}

type SearchRadius = {
  latitude: number
  longitude: number
  radiusMeters: number
}

type FoodMapProps = {
  actionOverlay?: ReactNode
  listings: FoodListing[]
  draggableMarker?: DraggableMarker
  height?: number | string
  initialLatitude?: number
  initialLongitude?: number
  initialZoom?: number
  onMoveEnd?: (latitude: number, longitude: number) => void
  previewRadius?: SearchRadius
  searchRadius?: SearchRadius
  searchRadiusFitKey?: string
}

const radiusFillLayer: LayerProps = {
  id: 'search-radius-fill',
  type: 'fill',
  paint: {
    'fill-color': '#2fb344',
    'fill-opacity': 0.035,
    'fill-opacity-transition': {
      duration: 250,
    },
  },
}

const radiusOutlineLayer: LayerProps = {
  id: 'search-radius-outline',
  type: 'line',
  paint: {
    'line-color': '#2fb344',
    'line-opacity': 0.6,
    'line-width': 2.5,
    'line-opacity-transition': {
      duration: 250,
    },
  },
}

const previewRadiusOutlineLayer: LayerProps = {
  id: 'preview-search-radius-outline',
  type: 'line',
  paint: {
    'line-color': '#111827',
    'line-dasharray': [2, 2],
    'line-opacity': 0.48,
    'line-width': 2,
    'line-opacity-transition': {
      duration: 180,
    },
  },
}

const radiusAnimationDurationMs = 520
const radiusFitDurationMs = 650
const previewRadiusAnimationDurationMs = 260

const easeOutCubic = (progress: number) => 1 - (1 - progress) ** 3

const lerp = (start: number, end: number, progress: number) =>
  start + (end - start) * progress

const prefersReducedMotion = () =>
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

function useAnimatedSearchRadius(
  targetRadius: SearchRadius | undefined,
  durationMs: number,
) {
  const animationFrameRef = useRef<number | null>(null)
  const [displayRadius, setDisplayRadius] = useState(targetRadius)

  useEffect(() => {
    if (!targetRadius) {
      setDisplayRadius(undefined)
      return undefined
    }

    if (!displayRadius || prefersReducedMotion()) {
      setDisplayRadius(targetRadius)
      return undefined
    }

    const start = displayRadius
    const startedAt = performance.now()

    const animateRadius = (now: number) => {
      const progress = Math.min(1, (now - startedAt) / durationMs)
      const easedProgress = easeOutCubic(progress)

      setDisplayRadius({
        latitude: lerp(start.latitude, targetRadius.latitude, easedProgress),
        longitude: lerp(start.longitude, targetRadius.longitude, easedProgress),
        radiusMeters: lerp(
          start.radiusMeters,
          targetRadius.radiusMeters,
          easedProgress,
        ),
      })

      if (progress < 1) {
        animationFrameRef.current = window.requestAnimationFrame(animateRadius)
      }
    }

    if (animationFrameRef.current !== null) {
      window.cancelAnimationFrame(animationFrameRef.current)
    }

    animationFrameRef.current = window.requestAnimationFrame(animateRadius)

    return () => {
      if (animationFrameRef.current !== null) {
        window.cancelAnimationFrame(animationFrameRef.current)
      }
    }
  }, [durationMs, targetRadius])

  return displayRadius
}

/**
 * Builds an approximate geodesic circle as GeoJSON for MapLibre.
 *
 * MapLibre's style layers draw GeoJSON polygons, not meter-based circles, so
 * this creates a small polygon around the query center. Sixty-four points is
 * enough to look smooth without making every radius change expensive.
 */
function createRadiusFeatureCollection({
  latitude,
  longitude,
  radiusMeters,
}: SearchRadius): FeatureCollection {
  const earthRadiusMeters = 6_371_000
  const centerLatitude = (latitude * Math.PI) / 180
  const centerLongitude = (longitude * Math.PI) / 180
  const angularDistance = radiusMeters / earthRadiusMeters
  const coordinates: Position[] = []

  for (let index = 0; index <= 64; index += 1) {
    const bearing = (2 * Math.PI * index) / 64
    const pointLatitude = Math.asin(
      Math.sin(centerLatitude) * Math.cos(angularDistance) +
        Math.cos(centerLatitude) * Math.sin(angularDistance) * Math.cos(bearing),
    )
    const pointLongitude =
      centerLongitude +
      Math.atan2(
        Math.sin(bearing) *
          Math.sin(angularDistance) *
          Math.cos(centerLatitude),
        Math.cos(angularDistance) -
          Math.sin(centerLatitude) * Math.sin(pointLatitude),
      )

    coordinates.push([
      (pointLongitude * 180) / Math.PI,
      (pointLatitude * 180) / Math.PI,
    ])
  }

  return {
    type: 'FeatureCollection',
    features: [
      {
        type: 'Feature',
        properties: {},
        geometry: {
          type: 'Polygon',
          coordinates: [coordinates],
        },
      },
    ],
  }
}

function createRadiusBounds({ latitude, longitude, radiusMeters }: SearchRadius) {
  const latitudeOffset = radiusMeters / 111_320
  const longitudeOffset =
    radiusMeters / (111_320 * Math.cos((latitude * Math.PI) / 180))

  return [
    [longitude - longitudeOffset, latitude - latitudeOffset],
    [longitude + longitudeOffset, latitude + latitudeOffset],
  ] as [[number, number], [number, number]]
}

/**
 * Shared MapLibre listing map.
 *
 * The map can run with the configured tile style or with the local empty style.
 * Markers are rendered from our API data, which lets frontend development keep
 * moving before the self-hosted tile service exists.
 */
export function FoodMap({
  actionOverlay,
  listings,
  draggableMarker,
  height = 520,
  initialLatitude = mapConfig.defaultCenter.latitude,
  initialLongitude = mapConfig.defaultCenter.longitude,
  initialZoom = mapConfig.defaultZoom,
  onMoveEnd,
  previewRadius,
  searchRadius,
  searchRadiusFitKey,
}: FoodMapProps) {
  const mapRef = useRef<MapRef>(null)
  const searchRadiusRef = useRef(searchRadius)
  const [selectedListing, setSelectedListing] = useState<FoodListing | null>(null)
  const displaySearchRadius = useAnimatedSearchRadius(
    searchRadius,
    radiusAnimationDurationMs,
  )
  const displayPreviewRadius = useAnimatedSearchRadius(
    previewRadius,
    previewRadiusAnimationDurationMs,
  )
  const radiusArea = useMemo(
    () =>
      displaySearchRadius
        ? createRadiusFeatureCollection(displaySearchRadius)
        : null,
    [displaySearchRadius],
  )
  const previewRadiusArea = useMemo(
    () =>
      displayPreviewRadius
        ? createRadiusFeatureCollection(displayPreviewRadius)
        : null,
    [displayPreviewRadius],
  )

  searchRadiusRef.current = searchRadius

  const fitSearchRadius = useCallback(() => {
    const radius = searchRadiusRef.current

    if (!radius || searchRadiusFitKey === undefined) {
      return
    }

    mapRef.current?.fitBounds(createRadiusBounds(radius), {
      duration: prefersReducedMotion() ? 0 : radiusFitDurationMs,
      easing: easeOutCubic,
      maxZoom: 13,
      padding: 44,
    })
  }, [searchRadiusFitKey])

  useEffect(() => {
    fitSearchRadius()
  }, [fitSearchRadius])

  const handleMoveEnd = (event: ViewStateChangeEvent) => {
    onMoveEnd?.(event.viewState.latitude, event.viewState.longitude)
  }

  const handleMarkerDragEnd = (event: MarkerDragEvent) => {
    draggableMarker?.onDragEnd(event.lngLat.lat, event.lngLat.lng)
  }

  return (
    <Paper
      h={height}
      miw={0}
      pos="relative"
      radius="sm"
      style={{ overflow: 'hidden' }}
      withBorder
    >
      {actionOverlay ? (
        <Paper
          p="xs"
          pos="absolute"
          radius="xl"
          shadow="md"
          style={{
            left: '50%',
            top: 12,
            transform: 'translateX(-50%)',
            zIndex: 2,
          }}
          withBorder
        >
          {actionOverlay}
        </Paper>
      ) : null}
      <Map
        ref={mapRef}
        initialViewState={{
          latitude: initialLatitude,
          longitude: initialLongitude,
          zoom: initialZoom,
        }}
        mapStyle={mapConfig.style}
        style={{ height: '100%', width: '100%' }}
        onLoad={fitSearchRadius}
        onMoveEnd={handleMoveEnd}
      >
        {radiusArea ? (
          <Source data={radiusArea} id="search-radius" type="geojson">
            <Layer {...radiusFillLayer} />
            <Layer {...radiusOutlineLayer} />
          </Source>
        ) : null}

        {previewRadiusArea ? (
          <Source
            data={previewRadiusArea}
            id="preview-search-radius"
            type="geojson"
          >
            <Layer {...previewRadiusOutlineLayer} />
          </Source>
        ) : null}

        {listings.map((listing, index) => {
          const location = listing.pickupLocation

          if (
            location?.latitude === undefined ||
            location.longitude === undefined
          ) {
            return null
          }

          const statusStyle = getListingStatusStyle(listing.status)

          return (
            <Marker
              key={listing.listingId ?? `${location.latitude}-${location.longitude}-${index}`}
              latitude={location.latitude}
              longitude={location.longitude}
              onClick={(event) => {
                event.originalEvent.stopPropagation()
                setSelectedListing(listing)
              }}
            >
              <Button
                aria-label={`Open ${listing.title ?? 'listing'}`}
                color={statusStyle.markerColor}
                h={36}
                p={0}
                radius="xl"
                variant="filled"
                w={36}
              >
                <MapPin size={18} />
              </Button>
            </Marker>
          )
        })}

        {draggableMarker ? (
          <Marker
            draggable
            latitude={draggableMarker.latitude}
            longitude={draggableMarker.longitude}
            onDragEnd={handleMarkerDragEnd}
          >
            <Button
              aria-label={draggableMarker.label}
              color="orange"
              h={40}
              p={0}
              radius="xl"
              variant="filled"
              w={40}
            >
              <MapPin size={20} />
            </Button>
          </Marker>
        ) : null}

        {selectedListing?.pickupLocation?.latitude !== undefined &&
        selectedListing.pickupLocation.longitude !== undefined ? (
          <Popup
            closeButton
            anchor="top"
            latitude={selectedListing.pickupLocation.latitude}
            longitude={selectedListing.pickupLocation.longitude}
            onClose={() => setSelectedListing(null)}
          >
            <Stack gap="xs" p="sm" maw={260}>
              <Group gap="xs">
                <Text fw={700} size="sm">
                  {selectedListing.title}
                </Text>
                {selectedListing.status ? (
                  <Badge
                    color={getListingStatusStyle(selectedListing.status).badgeColor}
                    size="xs"
                    variant="light"
                  >
                    {selectedListing.status}
                  </Badge>
                ) : null}
              </Group>
              <Text c="dimmed" lineClamp={2} size="sm">
                {selectedListing.description || 'No description provided.'}
              </Text>
              {selectedListing.listingId ? (
                <Button
                  component={Link}
                  to={routes.listingDetail(selectedListing.listingId)}
                  size="xs"
                >
                  View listing
                </Button>
              ) : null}
            </Stack>
          </Popup>
        ) : null}
      </Map>
    </Paper>
  )
}
