/**
 * Stable TanStack Query keys.
 *
 * Keeping keys centralized prevents typo-driven cache misses and makes
 * mutation invalidation obvious when a feature changes server state.
 */
export const queryKeys = {
  currentUser: ['current-user'] as const,
  listingsRoot: ['listings'] as const,
  listing: (listingId: string) =>
    [...queryKeys.listingsRoot, 'detail', listingId] as const,
  nearbyListings: (latitude: number, longitude: number, radiusMeters: number) =>
    [
      ...queryKeys.listingsRoot,
      'nearby',
      latitude,
      longitude,
      radiusMeters,
    ] as const,
  myListings: [...queryKeys.listingsRoot, 'mine'] as const,
  myReservations: ['my-reservations'] as const,
  listingReservations: (listingId: string) =>
    [...queryKeys.listingsRoot, 'reservations', listingId] as const,
}
