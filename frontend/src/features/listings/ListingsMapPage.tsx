import {
  Alert,
  Button,
  Grid,
  Group,
  Loader,
  NumberInput,
  ScrollArea,
  Stack,
  Text,
  Title,
} from '@mantine/core'
import { LocateFixed } from 'lucide-react'
import { useEffect } from 'react'
import { mapConfig } from '../../config/map'
import { FoodMap } from '../map/FoodMap'
import { ListingCard } from './components/ListingCard'
import { useNearbyListings } from './hooks'
import { useLocationStore } from '../../stores/locationStore'
import { formatMeters } from '../../shared/utils/format'

/**
 * Public landing page showing nearby food listings.
 */
export function ListingsMapPage() {
  const { location, radiusMeters, setLocation, setRadiusMeters } =
    useLocationStore()
  const listings = useNearbyListings()

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
    })
  }

  return (
    <Stack gap="md" py="md">
      <Group justify="space-between">
        <Stack gap={2}>
          <Title order={1}>Nearby food</Title>
          <Text c="dimmed">
            Searching within {formatMeters(radiusMeters)} of{' '}
            {location.source === 'fallback' ? 'Toronto' : 'your map center'}.
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
            listings={listings.data ?? []}
            initialLatitude={location.latitude}
            initialLongitude={location.longitude}
            onMoveEnd={(latitude, longitude) =>
              setLocation({ latitude, longitude, source: 'manual' })
            }
          />
        </Grid.Col>
        <Grid.Col span={{ base: 12, md: 4 }}>
          <Stack>
            <NumberInput
              clampBehavior="strict"
              label="Radius"
              max={mapConfig.maxRadiusMeters / 1000}
              min={1}
              suffix=" km"
              value={Math.round(radiusMeters / 1000)}
              onChange={(value) => {
                if (typeof value === 'number') {
                  setRadiusMeters(value * 1000)
                }
              }}
            />

            {listings.isLoading ? <Loader /> : null}
            {listings.isError ? (
              <Alert color="red">Could not load nearby listings.</Alert>
            ) : null}

            <ScrollArea.Autosize mah={460}>
              <Stack gap="sm">
                {(listings.data ?? []).map((listing) =>
                  listing.listingId ? (
                    <ListingCard key={listing.listingId} listing={listing} />
                  ) : null,
                )}
                {listings.isSuccess && listings.data.length === 0 ? (
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
