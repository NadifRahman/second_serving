import { apiRequest } from '../../api/client'
import type {
  CreateFoodListingRequest,
  FoodListing,
  PatchFoodListingRequest,
  Reservation,
} from '../../api/types'
import { apiPaths } from '../../config/apiPaths'

export type NearbyListingsParams = {
  latitude: number
  longitude: number
  radiusMeters: number
}

export function getNearbyListings(params: NearbyListingsParams, signal?: AbortSignal) {
  const searchParams = new URLSearchParams({
    latitude: String(params.latitude),
    longitude: String(params.longitude),
    radiusMeters: String(params.radiusMeters),
  })

  return apiRequest<FoodListing[]>(`${apiPaths.foodListings.nearby}?${searchParams}`, {
    signal,
  })
}

export function getListing(listingId: string, signal?: AbortSignal) {
  return apiRequest<FoodListing>(apiPaths.foodListings.detail(listingId), {
    signal,
  })
}

export function getMyListings(signal?: AbortSignal) {
  return apiRequest<FoodListing[]>(apiPaths.users.myFoodListings, { signal })
}

export function createListing(request: CreateFoodListingRequest) {
  return apiRequest<FoodListing, CreateFoodListingRequest>(
    apiPaths.users.myFoodListings,
    {
      method: 'POST',
      body: request,
    },
  )
}

export function updateListing(listingId: string, request: PatchFoodListingRequest) {
  return apiRequest<FoodListing, PatchFoodListingRequest>(
    apiPaths.users.myFoodListing(listingId),
    {
      method: 'PATCH',
      body: request,
    },
  )
}

export function deleteListing(listingId: string) {
  return apiRequest<void>(apiPaths.users.myFoodListing(listingId), {
    method: 'DELETE',
  })
}

export function getListingReservations(listingId: string, signal?: AbortSignal) {
  return apiRequest<Reservation[]>(
    apiPaths.users.myFoodListingReservations(listingId),
    { signal },
  )
}
