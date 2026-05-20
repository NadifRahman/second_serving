import { apiRequest } from '../../api/client'
import type {
  CreateFoodListingRequest,
  FoodListing,
  PatchFoodListingRequest,
  Reservation,
} from '../../api/types'

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

  return apiRequest<FoodListing[]>(`/api/food-listings/nearby?${searchParams}`, {
    signal,
  })
}

export function getListing(listingId: string, signal?: AbortSignal) {
  return apiRequest<FoodListing>(`/api/food-listings/${listingId}`, { signal })
}

export function getMyListings(signal?: AbortSignal) {
  return apiRequest<FoodListing[]>('/api/users/me/food-listings', { signal })
}

export function createListing(request: CreateFoodListingRequest) {
  return apiRequest<FoodListing, CreateFoodListingRequest>(
    '/api/users/me/food-listings',
    {
      method: 'POST',
      body: request,
    },
  )
}

export function updateListing(listingId: string, request: PatchFoodListingRequest) {
  return apiRequest<FoodListing, PatchFoodListingRequest>(
    `/api/users/me/food-listings/${listingId}`,
    {
      method: 'PATCH',
      body: request,
    },
  )
}

export function deleteListing(listingId: string) {
  return apiRequest<void>(`/api/users/me/food-listings/${listingId}`, {
    method: 'DELETE',
  })
}

export function getListingReservations(listingId: string, signal?: AbortSignal) {
  return apiRequest<Reservation[]>(
    `/api/users/me/food-listings/${listingId}/reservations`,
    { signal },
  )
}
