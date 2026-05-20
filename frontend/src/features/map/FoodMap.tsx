import { Badge, Button, Group, Paper, Stack, Text } from '@mantine/core'
import { MapPin } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import Map, {
  Marker,
  Popup,
  type MarkerDragEvent,
  type ViewStateChangeEvent,
} from 'react-map-gl/maplibre'
import type { FoodListing } from '../../api/types'
import { mapConfig } from '../../config/map'
import { routes } from '../../config/routes'

type DraggableMarker = {
  latitude: number
  longitude: number
  label: string
  onDragEnd: (latitude: number, longitude: number) => void
}

type FoodMapProps = {
  listings: FoodListing[]
  draggableMarker?: DraggableMarker
  height?: number | string
  initialLatitude?: number
  initialLongitude?: number
  initialZoom?: number
  onMoveEnd?: (latitude: number, longitude: number) => void
}

/**
 * Shared MapLibre listing map.
 *
 * The map can run with the configured tile style or with the local empty style.
 * Markers are rendered from our API data, which lets frontend development keep
 * moving before the self-hosted tile service exists.
 */
export function FoodMap({
  listings,
  draggableMarker,
  height = 520,
  initialLatitude = mapConfig.defaultCenter.latitude,
  initialLongitude = mapConfig.defaultCenter.longitude,
  initialZoom = mapConfig.defaultZoom,
  onMoveEnd,
}: FoodMapProps) {
  const [selectedListing, setSelectedListing] = useState<FoodListing | null>(null)

  const handleMoveEnd = (event: ViewStateChangeEvent) => {
    onMoveEnd?.(event.viewState.latitude, event.viewState.longitude)
  }

  const handleMarkerDragEnd = (event: MarkerDragEvent) => {
    draggableMarker?.onDragEnd(event.lngLat.lat, event.lngLat.lng)
  }

  return (
    <Paper h={height} miw={0} radius="sm" style={{ overflow: 'hidden' }} withBorder>
      <Map
        initialViewState={{
          latitude: initialLatitude,
          longitude: initialLongitude,
          zoom: initialZoom,
        }}
        mapStyle={mapConfig.style}
        style={{ height: '100%', width: '100%' }}
        onMoveEnd={handleMoveEnd}
      >
        {listings.map((listing, index) => {
          const location = listing.pickupLocation

          if (
            location?.latitude === undefined ||
            location.longitude === undefined
          ) {
            return null
          }

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
                  <Badge size="xs" variant="light">
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
