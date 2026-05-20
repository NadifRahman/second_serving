/**
 * Stable TanStack Query keys.
 *
 * Keeping keys centralized prevents typo-driven cache misses and makes
 * mutation invalidation obvious when a feature changes server state.
 */
export const queryKeys = {
  currentUser: ['current-user'] as const,
  listing: (listingId: string) => ['listing', listingId] as const,
  nearbyListings: (latitude: number, longitude: number, radiusMeters: number) =>
    ['listings', 'nearby', latitude, longitude, radiusMeters] as const,
  myListings: ['my-listings'] as const,
  myReservations: ['my-reservations'] as const,
  listingReservations: (listingId: string) =>
    ['listing-reservations', listingId] as const,
}
