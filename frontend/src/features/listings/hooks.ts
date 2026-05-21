import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import type {
  CreateFoodListingRequest,
  FoodListing,
  PatchFoodListingRequest,
} from '../../api/types'
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
        await queryClient.invalidateQueries({ queryKey: queryKeys.listingsRoot })
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
        if (listing.listingId) {
          queryClient.setQueryData(queryKeys.listing(listing.listingId), listing)
        }
        await queryClient.invalidateQueries({ queryKey: queryKeys.listingsRoot })
      },
    }),
    remove: useMutation({
      mutationFn: deleteListing,
      onSuccess: async (_deleted, listingId) => {
        queryClient.setQueryData<FoodListing[]>(
          queryKeys.myListings,
          (listings) =>
            listings?.filter((listing) => listing.listingId !== listingId) ?? [],
        )
        queryClient.removeQueries({ queryKey: queryKeys.listing(listingId) })
        queryClient
          .getQueriesData<FoodListing[]>({ queryKey: queryKeys.listingsRoot })
          .forEach(([queryKey, listings]) => {
            if (!listings) {
              return
            }

            queryClient.setQueryData(
              queryKey,
              listings.filter((listing) => listing.listingId !== listingId),
            )
          })
        await queryClient.invalidateQueries({ queryKey: queryKeys.listingsRoot })
      },
    }),
  }
}

export type ListingFormMutationInput = CreateFoodListingRequest
