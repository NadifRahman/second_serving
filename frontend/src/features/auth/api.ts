import { apiRequest } from '../../api/client'
import type { AuthSession, CurrentUser, LoginRequest, SignupRequest } from '../../api/types'
import { apiPaths } from '../../config/apiPaths'

export function getCurrentUser(signal?: AbortSignal) {
  return apiRequest<CurrentUser>(apiPaths.users.me, { signal })
}

export function login(request: LoginRequest) {
  return apiRequest<AuthSession, LoginRequest>(apiPaths.auth.login, {
    method: 'POST',
    body: request,
  })
}

export function signup(request: SignupRequest) {
  return apiRequest<AuthSession, SignupRequest>(apiPaths.auth.signup, {
    method: 'POST',
    body: request,
  })
}

export function logout() {
  return apiRequest<void>(apiPaths.auth.logout, { method: 'POST' })
}
