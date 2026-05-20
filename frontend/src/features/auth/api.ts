import { apiRequest } from '../../api/client'
import type { AuthSession, CurrentUser, LoginRequest, SignupRequest } from '../../api/types'

export function getCurrentUser(signal?: AbortSignal) {
  return apiRequest<CurrentUser>('/api/users/me', { signal })
}

export function login(request: LoginRequest) {
  return apiRequest<AuthSession, LoginRequest>('/api/login', {
    method: 'POST',
    body: request,
  })
}

export function signup(request: SignupRequest) {
  return apiRequest<AuthSession, SignupRequest>('/api/signup', {
    method: 'POST',
    body: request,
  })
}

export function logout() {
  return apiRequest<void>('/api/logout', { method: 'POST' })
}
