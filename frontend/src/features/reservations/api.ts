import { apiRequest } from '../../api/client'
import type {
  CreateReservationRequest,
  PatchReservationRequest,
  Reservation,
} from '../../api/types'
import { apiPaths } from '../../config/apiPaths'

export function getMyReservations(signal?: AbortSignal) {
  return apiRequest<Reservation[]>(apiPaths.users.myReservations, { signal })
}

export function createReservation(request: CreateReservationRequest) {
  return apiRequest<Reservation, CreateReservationRequest>(
    apiPaths.users.myReservations,
    {
      method: 'POST',
      body: request,
    },
  )
}

export function updateReservation(
  listingId: string,
  request: PatchReservationRequest,
) {
  return apiRequest<Reservation, PatchReservationRequest>(
    apiPaths.users.myReservation(listingId),
    {
      method: 'PATCH',
      body: request,
    },
  )
}

export function deleteReservation(listingId: string) {
  return apiRequest<void>(apiPaths.users.myReservation(listingId), {
    method: 'DELETE',
  })
}
