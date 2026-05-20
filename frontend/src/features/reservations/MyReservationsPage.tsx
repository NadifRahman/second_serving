import { Badge, Button, Group, Paper, Stack, Text, Title } from '@mantine/core'
import { Link } from 'react-router-dom'
import { routes } from '../../config/routes'
import { EmptyState } from '../../shared/components/EmptyState'
import { formatDateTime } from '../../shared/utils/format'
import { useMyReservations, useReservationMutations } from './hooks'

/**
 * Authenticated requester reservation list.
 */
export function MyReservationsPage() {
  const reservations = useMyReservations()
  const mutations = useReservationMutations()

  return (
    <Stack py="md">
      <Title order={1}>My reservations</Title>

      <Stack gap="sm">
        {(reservations.data ?? []).map((reservation) => {
          const listing = reservation.foodListing
          const listingId = reservation.listingId

          return (
            <Paper key={listingId} withBorder p="md" radius="sm">
              <Stack gap="xs">
                <Group justify="space-between">
                  <Title order={3} size="h4">
                    {listing?.title ?? 'Listing'}
                  </Title>
                  {reservation.reservationStatus ? (
                    <Badge variant="light">{reservation.reservationStatus}</Badge>
                  ) : null}
                </Group>
                <Text c="dimmed" size="sm">
                  Reserved {reservation.quantityRequested}{' '}
                  {listing?.quantityUnit?.toLowerCase()}
                </Text>
                <Text c="dimmed" size="sm">
                  Created {formatDateTime(reservation.createdAt)}
                </Text>
                <Group gap="xs">
                  {listingId ? (
                    <>
                      <Button
                        component={Link}
                        size="xs"
                        to={routes.listingDetail(listingId)}
                      >
                        View listing
                      </Button>
                      <Button
                        color="red"
                        loading={mutations.remove.isPending}
                        size="xs"
                        variant="light"
                        onClick={() => mutations.remove.mutate(listingId)}
                      >
                        Delete
                      </Button>
                    </>
                  ) : null}
                </Group>
              </Stack>
            </Paper>
          )
        })}
      </Stack>

      {reservations.isSuccess && reservations.data.length === 0 ? (
        <EmptyState
          message="Reservations you make on listings will appear here."
          title="No reservations yet"
        />
      ) : null}
    </Stack>
  )
}
