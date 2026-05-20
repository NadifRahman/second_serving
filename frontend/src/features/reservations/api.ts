import { apiRequest } from '../../api/client'
import type {
  CreateReservationRequest,
  PatchReservationRequest,
  Reservation,
} from '../../api/types'

export function getMyReservations(signal?: AbortSignal) {
  return apiRequest<Reservation[]>('/api/users/me/reservations', { signal })
}

export function createReservation(request: CreateReservationRequest) {
  return apiRequest<Reservation, CreateReservationRequest>(
    '/api/users/me/reservations',
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
    `/api/users/me/reservations/${listingId}`,
    {
      method: 'PATCH',
      body: request,
    },
  )
}

export function deleteReservation(listingId: string) {
  return apiRequest<void>(`/api/users/me/reservations/${listingId}`, {
    method: 'DELETE',
  })
}
