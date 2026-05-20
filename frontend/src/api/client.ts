import { env } from '../config/env'

export type ApiRequestOptions<TBody> = {
  body?: TBody
  method?: 'DELETE' | 'GET' | 'PATCH' | 'POST'
  signal?: AbortSignal
}

/**
 * Error object thrown for non-2xx API responses.
 *
 * Fetch only rejects on network failure, so the app needs a small explicit
 * error type for validation, auth, not-found, and conflict responses returned
 * by Spring.
 */
export class ApiError extends Error {
  readonly status: number
  readonly responseBody: string

  constructor(status: number, responseBody: string) {
    super(responseBody || `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.responseBody = responseBody
  }
}

/**
 * Sends a typed JSON request to the Spring API.
 *
 * Cookies are included because auth is represented by an HttpOnly JWT cookie.
 * The helper deliberately stays small; endpoint-specific modules provide the
 * generated OpenAPI types and query invalidation behavior.
 */
export async function apiRequest<TResponse, TBody = unknown>(
  path: string,
  options: ApiRequestOptions<TBody> = {},
): Promise<TResponse> {
  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    method: options.method ?? 'GET',
    credentials: 'include',
    headers:
      options.body === undefined
        ? undefined
        : {
            'Content-Type': 'application/json',
          },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    signal: options.signal,
  })

  if (!response.ok) {
    throw new ApiError(response.status, await response.text())
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  return response.json() as Promise<TResponse>
}
