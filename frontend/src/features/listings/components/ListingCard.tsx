import { Badge, Button, Group, Paper, Stack, Text, Title } from '@mantine/core'
import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import type { FoodListing } from '../../../api/types'
import { routes } from '../../../config/routes'
import { formatDateTime } from '../../../shared/utils/format'
import { getListingStatusStyle } from '../statusStyles'

type ListingCardProps = {
  listing: FoodListing
  action?: ReactNode
}

/**
 * Compact listing summary used by map results and owner lists.
 */
export function ListingCard({ listing, action }: ListingCardProps) {
  const statusStyle = getListingStatusStyle(listing.status)

  return (
    <Paper withBorder p="md" radius="sm">
      <Stack gap="xs">
        <Group justify="space-between" wrap="nowrap">
          <Title order={3} size="h4">
            {listing.title}
          </Title>
          {listing.status ? (
            <Badge color={statusStyle.badgeColor} variant="light">
              {listing.status}
            </Badge>
          ) : null}
        </Group>
        <Text c="dimmed" lineClamp={2} size="sm">
          {listing.description || 'No description provided.'}
        </Text>
        <Text size="sm">
          {listing.quantity} {listing.quantityUnit?.toLowerCase()} available
        </Text>
        <Text c="dimmed" size="sm">
          Expires {formatDateTime(listing.expiresAt)}
        </Text>
        <Group gap="xs">
          {listing.listingId ? (
            <Button component={Link} size="xs" to={routes.listingDetail(listing.listingId)}>
              View
            </Button>
          ) : null}
          {action}
        </Group>
      </Stack>
    </Paper>
  )
}
