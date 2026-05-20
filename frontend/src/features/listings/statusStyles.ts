import type { MantineColor } from '@mantine/core'
import type { ListingStatus } from '../../api/types'

type ListingStatusStyle = {
  badgeColor: MantineColor
  markerColor: MantineColor
}

const fallbackStatusStyle: ListingStatusStyle = {
  badgeColor: 'gray',
  markerColor: 'gray',
}

/**
 * Visual treatment for each food listing lifecycle state.
 *
 * The mapping is centralized so map markers, listing cards, and detail badges
 * communicate the same meaning everywhere. Available stays green because it is
 * actionable; finished is calmer blue; cancelled is red because the listing is
 * no longer usable.
 */
export const listingStatusStyles: Record<ListingStatus, ListingStatusStyle> = {
  AVAILABLE: {
    badgeColor: 'green',
    markerColor: 'green',
  },
  FINISHED: {
    badgeColor: 'blue',
    markerColor: 'blue',
  },
  CANCELLED: {
    badgeColor: 'red',
    markerColor: 'red',
  },
}

/**
 * Returns the configured visual style for a listing status.
 */
export function getListingStatusStyle(status: ListingStatus | undefined) {
  return status ? listingStatusStyles[status] : fallbackStatusStyle
}
