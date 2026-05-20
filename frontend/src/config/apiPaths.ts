/**
 * Backend API path constants.
 *
 * Feature API modules should build requests through this object instead of
 * repeating string literals. That keeps the frontend aligned with Spring's
 * controller path structure and makes endpoint changes easier to review.
 */
export const apiPaths = {
  auth: {
    login: '/api/login',
    logout: '/api/logout',
    signup: '/api/signup',
  },
  foodListings: {
    detail: (listingId: string) => `/api/food-listings/${listingId}`,
    nearby: '/api/food-listings/nearby',
  },
  users: {
    me: '/api/users/me',
    myFoodListings: '/api/users/me/food-listings',
    myFoodListing: (listingId: string) =>
      `/api/users/me/food-listings/${listingId}`,
    myFoodListingReservations: (listingId: string) =>
      `/api/users/me/food-listings/${listingId}/reservations`,
    myReservation: (listingId: string) =>
      `/api/users/me/reservations/${listingId}`,
    myReservations: '/api/users/me/reservations',
  },
} as const
