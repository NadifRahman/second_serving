import {
  Alert,
  Button,
  Grid,
  Group,
  Loader,
  ScrollArea,
  Select,
  Slider,
  Stack,
  Text,
  Title,
} from '@mantine/core'
import { LocateFixed, Search } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import type { ListingStatus } from '../../api/types'
import { FoodMap } from '../map/FoodMap'
import { ListingCard } from './components/ListingCard'
import { useNearbyListings } from './hooks'
import { useLocationStore, type UserLocation } from '../../stores/locationStore'
import { distanceInMeters, formatMeters } from '../../shared/utils/format'
import { listingStatusOptions } from './schemas'

const allStatusesFilter = 'ALL'

const mapStatusFilterOptions = [
  { label: 'All', value: allStatusesFilter },
  ...listingStatusOptions,
]

const minimumSearchMoveMeters = 250
const radiusKilometerOptions = [
  1, 2, 3, 4, 5, 7, 10, 15, 20, 25, 30, 40, 50, 75, 100,
] as const
const radiusMarkIndexes = [0, 4, 8, 12, 14] as const

const radiusPresetMarks = radiusMarkIndexes.map((index) => ({
  label: `${radiusKilometerOptions[index]} km`,
  value: index,
}))

const radiusKilometersFromIndex = (index: number) =>
  radiusKilometerOptions[index]

const radiusMetersFromIndex = (index: number) =>
  radiusKilometersFromIndex(index) * 1000

const radiusIndexFromMeters = (value: number) => {
  const kilometers = value / 1000

  return radiusKilometerOptions.reduce((nearestIndex, currentValue, index) => {
    const nearestDelta = Math.abs(
      radiusKilometerOptions[nearestIndex] - kilometers,
    )
    const currentDelta = Math.abs(currentValue - kilometers)

    return currentDelta < nearestDelta ? index : nearestIndex
  }, 0)
}

/**
 * Public landing page showing nearby food listings.
 */
export function ListingsMapPage() {
  const { location, radiusMeters, setLocation, setRadiusMeters } =
    useLocationStore()
  const [draftMapCenter, setDraftMapCenter] = useState<UserLocation>(location)
  const [draftRadiusIndex, setDraftRadiusIndex] = useState(
    radiusIndexFromMeters(radiusMeters),
  )
  const [radiusFitVersion, setRadiusFitVersion] = useState(0)
  const [statusFilter, setStatusFilter] = useState<ListingStatus | 'ALL'>(
    allStatusesFilter,
  )
  const listings = useNearbyListings()
  const draftRadiusMeters = radiusMetersFromIndex(draftRadiusIndex)
  const mapMovedMeters = distanceInMeters(location, draftMapCenter)
  const searchMoveThresholdMeters = Math.max(
    minimumSearchMoveMeters,
    radiusMeters * 0.1,
  )
  const radiusChanged = draftRadiusMeters !== radiusMeters
  const canSearchDraftArea =
    radiusChanged || mapMovedMeters >= searchMoveThresholdMeters
  const filteredListings = useMemo(() => {
    if (statusFilter === allStatusesFilter) {
      return listings.data ?? []
    }

    return (listings.data ?? []).filter((listing) => listing.status === statusFilter)
  }, [listings.data, statusFilter])

  useEffect(() => {
    if (!navigator.geolocation || location.source !== 'fallback') {
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          source: 'browser',
        })
        setDraftMapCenter({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          source: 'browser',
        })
        setDraftRadiusIndex(radiusIndexFromMeters(radiusMeters))
        setRadiusFitVersion((version) => version + 1)
      },
      () => undefined,
      { enableHighAccuracy: true, maximumAge: 300_000, timeout: 8_000 },
    )
  }, [location.source, setLocation])

  const useBrowserLocation = () => {
    navigator.geolocation?.getCurrentPosition((position) => {
      setLocation({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        source: 'browser',
      })
      setDraftMapCenter({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        source: 'browser',
      })
      setDraftRadiusIndex(radiusIndexFromMeters(radiusMeters))
      setRadiusFitVersion((version) => version + 1)
    })
  }

  const searchDraftArea = () => {
    setRadiusMeters(draftRadiusMeters)
    setLocation({ ...draftMapCenter, source: 'manual' })
    setRadiusFitVersion((version) => version + 1)
  }

  return (
    <Stack gap="md" py="md">
      <Group justify="space-between">
        <Stack gap={2}>
          <Title order={1}>Nearby food</Title>
          <Text c="dimmed">
            Showing results within {formatMeters(radiusMeters)} of{' '}
            {location.source === 'fallback' ? 'Toronto' : 'the searched area'}.
          </Text>
        </Stack>
        <Button
          leftSection={<LocateFixed size={16} />}
          variant="light"
          onClick={useBrowserLocation}
        >
          Use my location
        </Button>
      </Group>

      <Grid>
        <Grid.Col span={{ base: 12, md: 8 }}>
          <FoodMap
            actionOverlay={
              canSearchDraftArea ? (
                <Button
                  leftSection={<Search size={16} />}
                  radius="xl"
                  size="sm"
                  onClick={searchDraftArea}
                >
                  Search this area
                </Button>
              ) : undefined
            }
            listings={filteredListings}
            initialLatitude={location.latitude}
            initialLongitude={location.longitude}
            previewRadius={
              canSearchDraftArea
                ? {
                    latitude: draftMapCenter.latitude,
                    longitude: draftMapCenter.longitude,
                    radiusMeters: draftRadiusMeters,
                  }
                : undefined
            }
            searchRadius={{
              latitude: location.latitude,
              longitude: location.longitude,
              radiusMeters,
            }}
            searchRadiusFitKey={String(radiusFitVersion)}
            onMoveEnd={(latitude, longitude) =>
              setDraftMapCenter({ latitude, longitude, source: 'manual' })
            }
          />
        </Grid.Col>
        <Grid.Col span={{ base: 12, md: 4 }}>
          <Stack>
            <Slider
              label="Radius"
              labelAlwaysOn
              label={(value) => `${radiusKilometersFromIndex(value)} km`}
              marks={radiusPresetMarks}
              max={radiusKilometerOptions.length - 1}
              min={0}
              step={1}
              value={draftRadiusIndex}
              onChange={setDraftRadiusIndex}
            />

            <Select
              data={mapStatusFilterOptions}
              label="Status"
              value={statusFilter}
              onChange={(value) =>
                setStatusFilter((value ?? allStatusesFilter) as ListingStatus | 'ALL')
              }
            />

            {listings.isLoading ? <Loader /> : null}
            {listings.isError ? (
              <Alert color="red">Could not load nearby listings.</Alert>
            ) : null}

            <ScrollArea.Autosize mah={460}>
              <Stack gap="sm">
                {filteredListings.map((listing) =>
                  listing.listingId ? (
                    <ListingCard key={listing.listingId} listing={listing} />
                  ) : null,
                )}
                {listings.isSuccess && filteredListings.length === 0 ? (
                  <Text c="dimmed" size="sm">
                    No listings found in this area.
                  </Text>
                ) : null}
              </Stack>
            </ScrollArea.Autosize>
          </Stack>
        </Grid.Col>
      </Grid>
    </Stack>
  )
}
