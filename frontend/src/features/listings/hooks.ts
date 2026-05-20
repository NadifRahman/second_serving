import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import type { CreateFoodListingRequest, PatchFoodListingRequest } from '../../api/types'
import { useLocationStore } from '../../stores/locationStore'
import {
  createListing,
  deleteListing,
  getListing,
  getListingReservations,
  getMyListings,
  getNearbyListings,
  updateListing,
} from './api'

export function useNearbyListings() {
  const location = useLocationStore((state) => state.location)
  const radiusMeters = useLocationStore((state) => state.radiusMeters)

  return useQuery({
    queryKey: queryKeys.nearbyListings(
      location.latitude,
      location.longitude,
      radiusMeters,
    ),
    queryFn: ({ signal }) =>
      getNearbyListings(
        {
          latitude: location.latitude,
          longitude: location.longitude,
          radiusMeters,
        },
        signal,
      ),
  })
}

export function useListing(listingId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.listing(listingId ?? 'missing'),
    queryFn: ({ signal }) => getListing(listingId ?? '', signal),
    enabled: Boolean(listingId),
  })
}

export function useMyListings() {
  return useQuery({
    queryKey: queryKeys.myListings,
    queryFn: ({ signal }) => getMyListings(signal),
  })
}

export function useListingReservations(listingId: string | undefined, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.listingReservations(listingId ?? 'missing'),
    queryFn: ({ signal }) => getListingReservations(listingId ?? '', signal),
    enabled: Boolean(listingId) && enabled,
  })
}

/**
 * Listing mutations with cache invalidation for the pages affected by changes.
 */
export function useListingMutations() {
  const queryClient = useQueryClient()

  return {
    create: useMutation({
      mutationFn: createListing,
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: queryKeys.myListings })
      },
    }),
    update: useMutation({
      mutationFn: ({
        listingId,
        request,
      }: {
        listingId: string
        request: PatchFoodListingRequest
      }) => updateListing(listingId, request),
      onSuccess: async (listing) => {
        await queryClient.invalidateQueries({ queryKey: queryKeys.myListings })
        if (listing.listingId) {
          await queryClient.invalidateQueries({
            queryKey: queryKeys.listing(listing.listingId),
          })
        }
      },
    }),
    remove: useMutation({
      mutationFn: deleteListing,
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: queryKeys.myListings })
      },
    }),
  }
}

export type ListingFormMutationInput = CreateFoodListingRequest
