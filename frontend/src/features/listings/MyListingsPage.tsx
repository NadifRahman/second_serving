import {
  Button,
  Group,
  Modal,
  Select,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core'
import { Search, Trash2 } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import type { FoodListing } from '../../api/types'
import { routes } from '../../config/routes'
import { EmptyState } from '../../shared/components/EmptyState'
import { ListingCard } from './components/ListingCard'
import { useListingMutations, useMyListings } from './hooks'

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
  const [listingToDelete, setListingToDelete] = useState<FoodListing | null>(
    null,
  )
  const listings = useMyListings()
  const listingMutations = useListingMutations()

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

  const deleteSelectedListing = async () => {
    if (!listingToDelete?.listingId) {
      return
    }

    await listingMutations.remove.mutateAsync(listingToDelete.listingId)
    setListingToDelete(null)
  }

  return (
    <Stack py="md">
      <Modal
        centered
        opened={Boolean(listingToDelete)}
        title="Delete listing"
        onClose={() => setListingToDelete(null)}
      >
        <Stack>
          <Text>
            Delete {listingToDelete?.title ?? 'this listing'}? This cannot be
            undone.
          </Text>
          <Group justify="flex-end">
            <Button variant="subtle" onClick={() => setListingToDelete(null)}>
              Cancel
            </Button>
            <Button
              color="red"
              loading={listingMutations.remove.isPending}
              onClick={deleteSelectedListing}
            >
              Delete
            </Button>
          </Group>
        </Stack>
      </Modal>

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
                <>
                  <Button
                    component={Link}
                    size="xs"
                    to={routes.editListing(listing.listingId)}
                    variant="light"
                  >
                    Edit
                  </Button>
                  <Button
                    color="red"
                    leftSection={<Trash2 size={14} />}
                    size="xs"
                    variant="light"
                    onClick={() => setListingToDelete(listing)}
                  >
                    Delete
                  </Button>
                </>
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
