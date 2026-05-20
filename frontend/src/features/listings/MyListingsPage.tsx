import { Button, Group, Select, Stack, TextInput, Title } from '@mantine/core'
import { Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { routes } from '../../config/routes'
import { EmptyState } from '../../shared/components/EmptyState'
import { ListingCard } from './components/ListingCard'
import { useMyListings } from './hooks'

const sortOptions = [
  { label: 'Newest', value: 'newest' },
  { label: 'Expiring soon', value: 'expires' },
  { label: 'Title', value: 'title' },
] as const

/**
 * Authenticated owner's listing management page.
 */
export function MyListingsPage() {
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState<string>('newest')
  const listings = useMyListings()

  const visibleListings = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()
    const filtered = (listings.data ?? []).filter((listing) =>
      listing.title?.toLowerCase().includes(normalizedSearch),
    )

    return filtered.toSorted((a, b) => {
      if (sort === 'title') {
        return (a.title ?? '').localeCompare(b.title ?? '')
      }
      if (sort === 'expires') {
        return (a.expiresAt ?? '').localeCompare(b.expiresAt ?? '')
      }
      return (b.createdAt ?? '').localeCompare(a.createdAt ?? '')
    })
  }, [listings.data, search, sort])

  return (
    <Stack py="md">
      <Group justify="space-between">
        <Title order={1}>My listings</Title>
        <Button component={Link} to={routes.newListing}>
          New listing
        </Button>
      </Group>

      <Group align="end">
        <TextInput
          flex={1}
          leftSection={<Search size={16} />}
          label="Search"
          value={search}
          onChange={(event) => setSearch(event.currentTarget.value)}
        />
        <Select
          data={sortOptions}
          label="Sort"
          value={sort}
          onChange={(value) => setSort(value ?? 'newest')}
        />
      </Group>

      <Stack gap="sm">
        {visibleListings.map((listing) =>
          listing.listingId ? (
            <ListingCard
              key={listing.listingId}
              action={
                <Button
                  component={Link}
                  size="xs"
                  to={routes.editListing(listing.listingId)}
                  variant="light"
                >
                  Edit
                </Button>
              }
              listing={listing}
            />
          ) : null,
        )}
      </Stack>

      {listings.isSuccess && visibleListings.length === 0 ? (
        <EmptyState
          action={
            <Button component={Link} to={routes.newListing}>
              Create listing
            </Button>
          }
          message="Listings you create will appear here."
          title="No listings found"
        />
      ) : null}
    </Stack>
  )
}
