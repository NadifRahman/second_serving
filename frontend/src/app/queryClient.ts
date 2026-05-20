import { QueryClient } from '@tanstack/react-query'

/**
 * Shared TanStack Query client.
 *
 * The stale time prevents immediate refetch loops while navigating between
 * pages, but server state still refreshes often enough for a food sharing app
 * where listings and reservations can change quickly.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 30_000,
    },
  },
})
