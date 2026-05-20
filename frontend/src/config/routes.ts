/**
 * Central route constants used by navigation and redirects.
 *
 * Static paths live here so links stay consistent as the app grows. Dynamic
 * route builders are functions to avoid string interpolation mistakes in page
 * components.
 */
export const routes = {
  home: '/',
  signIn: '/sign-in',
  signUp: '/sign-up',
  newListing: '/listings/new',
  myListings: '/me/listings',
  myReservations: '/me/reservations',
  listingDetail: (listingId: string) => `/listings/${listingId}`,
  editListing: (listingId: string) => `/listings/${listingId}/edit`,
} as const
