import {
  Alert,
  Badge,
  Button,
  Divider,
  Grid,
  Group,
  Loader,
  NumberInput,
  Paper,
  Stack,
  Table,
  Text,
  Title,
} from '@mantine/core'
import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { routes } from '../../config/routes'
import { useAuthStore } from '../../stores/authStore'
import { formatDateTime } from '../../shared/utils/format'
import { FoodMap } from '../map/FoodMap'
import { useReservationMutations } from '../reservations/hooks'
import {
  useListing,
  useListingMutations,
  useListingReservations,
} from './hooks'
import {
  reservationFormSchema,
  type ReservationFormValues,
} from './schemas'
import { getListingStatusStyle } from './statusStyles'

/**
 * Public listing detail page with owner and requester actions.
 */
export function ListingDetailPage() {
  const { listingId } = useParams()
  const user = useAuthStore((state) => state.user)
  const listing = useListing(listingId)
  const listingMutations = useListingMutations()
  const reservationMutations = useReservationMutations(listingId)
  const reservationErrorMessage =
    reservationMutations.create.error instanceof ApiError
      ? reservationMutations.create.error.responseBody
      : 'Could not create this reservation.'
  const form = useForm<ReservationFormValues>({
    resolver: zodResolver(reservationFormSchema),
    defaultValues: { quantityRequested: 1 },
  })

  const isOwner =
    Boolean(user?.userId) &&
    Boolean(listing.data?.ownerId) &&
    user?.userId === listing.data?.ownerId
  const ownerReservations = useListingReservations(listingId, isOwner)

  if (listing.isLoading) {
    return <Loader mt="xl" />
  }

  if (listing.isError || !listing.data) {
    return <Alert color="red" mt="xl">Could not load this listing.</Alert>
  }

  const pickup = listing.data.pickupLocation
  const canReserve = Boolean(user) && !isOwner && Boolean(listingId)
  const statusStyle = getListingStatusStyle(listing.data.status)

  const handleReserve = async (values: ReservationFormValues) => {
    if (!listingId) {
      return
    }

    try {
      await reservationMutations.create.mutateAsync({
        listingId,
        quantityRequested: values.quantityRequested,
      })
    } catch {
      // Mutation state renders the backend's string error above the form.
    }
  }

  return (
    <Stack gap="lg" py="md">
      <Group justify="space-between" align="flex-start">
        <Stack gap={4}>
          <Group gap="xs">
            <Title order={1}>{listing.data.title}</Title>
            {listing.data.status ? (
              <Badge color={statusStyle.badgeColor}>{listing.data.status}</Badge>
            ) : null}
          </Group>
          <Text c="dimmed">Posted by {listing.data.ownerUsername}</Text>
        </Stack>
        {isOwner && listingId ? (
          <Group>
            <Button component={Link} to={routes.editListing(listingId)} variant="light">
              Edit
            </Button>
            <Button
              color="red"
              loading={listingMutations.remove.isPending}
              variant="light"
              onClick={() => listingMutations.remove.mutate(listingId)}
            >
              Delete
            </Button>
          </Group>
        ) : null}
      </Group>

      <Grid>
        <Grid.Col span={{ base: 12, md: 7 }}>
          <Stack>
            <Paper withBorder p="md" radius="sm">
              <Stack gap="sm">
                <Text>{listing.data.description || 'No description provided.'}</Text>
                <Divider />
                <Text>
                  {listing.data.quantity} {listing.data.quantityUnit?.toLowerCase()}{' '}
                  available
                </Text>
                <Text c="dimmed">Expires {formatDateTime(listing.data.expiresAt)}</Text>
                <Text c="dimmed">Pickup starts {formatDateTime(pickup?.pickupStartAt)}</Text>
                <Text c="dimmed">Pickup ends {formatDateTime(pickup?.pickupEndAt)}</Text>
                {pickup?.instructions ? <Text>{pickup.instructions}</Text> : null}
              </Stack>
            </Paper>

            {canReserve ? (
              <Paper withBorder p="md" radius="sm">
                <form onSubmit={form.handleSubmit(handleReserve)}>
                  <Stack>
                    <Title order={2}>Reserve</Title>
                    {reservationMutations.create.isError ? (
                      <Alert color="red">{reservationErrorMessage}</Alert>
                    ) : null}
                    <Controller
                      control={form.control}
                      name="quantityRequested"
                      render={({ field, fieldState }) => (
                        <NumberInput
                          required
                          error={fieldState.error?.message}
                          label="Quantity"
                          min={1}
                          value={field.value}
                          onChange={field.onChange}
                        />
                      )}
                    />
                    <Button
                      loading={reservationMutations.create.isPending}
                      type="submit"
                    >
                      Create reservation
                    </Button>
                  </Stack>
                </form>
              </Paper>
            ) : null}
          </Stack>
        </Grid.Col>
        <Grid.Col span={{ base: 12, md: 5 }}>
          <FoodMap
            height={360}
            initialLatitude={pickup?.latitude}
            initialLongitude={pickup?.longitude}
            initialZoom={14}
            listings={[listing.data]}
          />
        </Grid.Col>
      </Grid>

      {isOwner ? (
        <Paper withBorder p="md" radius="sm">
          <Stack>
            <Title order={2}>Reservations</Title>
            {ownerReservations.isLoading ? <Loader /> : null}
            {ownerReservations.data?.length ? (
              <Table>
                <Table.Thead>
                  <Table.Tr>
                    <Table.Th>User</Table.Th>
                    <Table.Th>Quantity</Table.Th>
                    <Table.Th>Status</Table.Th>
                  </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                  {ownerReservations.data.map((reservation) => (
                    <Table.Tr key={reservation.requesterId}>
                      <Table.Td>{reservation.requesterUsername}</Table.Td>
                      <Table.Td>{reservation.quantityRequested}</Table.Td>
                      <Table.Td>{reservation.reservationStatus}</Table.Td>
                    </Table.Tr>
                  ))}
                </Table.Tbody>
              </Table>
            ) : (
              <Text c="dimmed">No reservations yet.</Text>
            )}
          </Stack>
        </Paper>
      ) : null}
    </Stack>
  )
}
