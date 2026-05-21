const listingsRoot = ['listings'] as const

export const queryKeys = {
  currentUser: ['current-user'] as const,

  listingsRoot,

  listing: (listingId: string) =>
    [...listingsRoot, 'detail', listingId] as const,

  nearbyListings: (
    latitude: number,
    longitude: number,
    radiusMeters: number,
  ) =>
    [
      ...listingsRoot,
      'nearby',
      latitude,
      longitude,
      radiusMeters,
    ] as const,

  myListings: [...listingsRoot, 'mine'] as const,

  myReservations: ['my-reservations'] as const,

  listingReservations: (listingId: string) =>
    [...listingsRoot, 'reservations', listingId] as const,
}