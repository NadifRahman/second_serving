import type { components } from './schema'

export type AuthSession = components['schemas']['AuthSessionDto']
export type CreateFoodListingRequest =
  components['schemas']['CreateFoodListingRequestDto']
export type CreateReservationRequest =
  components['schemas']['CreateReservationRequestDto']
export type CurrentUser = components['schemas']['CurrentUserDto']
export type FoodListing = components['schemas']['FoodListingDto']
export type LoginRequest = components['schemas']['LoginRequestDto']
export type PatchFoodListingRequest =
  components['schemas']['PatchFoodListingRequestDto']
export type PatchReservationRequest =
  components['schemas']['PatchReservationDto']
export type Reservation = components['schemas']['ReservationDto']
export type SignupRequest = components['schemas']['SignupRequestDto']

export type ListingStatus = NonNullable<FoodListing['status']>
export type QuantityUnit = NonNullable<FoodListing['quantityUnit']>
export type ReservationStatus = NonNullable<Reservation['reservationStatus']>
