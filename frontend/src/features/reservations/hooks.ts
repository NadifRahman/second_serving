import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import type { CreateReservationRequest } from '../../api/types'
import {
  createReservation,
  deleteReservation,
  getMyReservations,
} from './api'

export function useMyReservations() {
  return useQuery({
    queryKey: queryKeys.myReservations,
    queryFn: ({ signal }) => getMyReservations(signal),
  })
}

/**
 * Reservation mutations invalidate requester and listing-owner reservation
 * views because both screens can reflect the same reservation state.
 */
export function useReservationMutations(listingId?: string) {
  const queryClient = useQueryClient()

  const invalidateReservations = async () => {
    await queryClient.invalidateQueries({ queryKey: queryKeys.myReservations })
    if (listingId) {
      await queryClient.invalidateQueries({
        queryKey: queryKeys.listingReservations(listingId),
      })
    }
  }

  return {
    create: useMutation({
      mutationFn: (request: CreateReservationRequest) => createReservation(request),
      onSuccess: invalidateReservations,
    }),
    remove: useMutation({
      mutationFn: deleteReservation,
      onSuccess: invalidateReservations,
    }),
  }
}
