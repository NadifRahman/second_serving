import { MantineProvider, createTheme } from '@mantine/core'
import { QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { queryClient } from './queryClient'

const theme = createTheme({
  primaryColor: 'green',
  defaultRadius: 'sm',
  fontFamily:
    'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
  headings: {
    fontFamily:
      'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
  },
})

type AppProvidersProps = {
  children: ReactNode
}

/**
 * Composes application-wide providers.
 *
 * Mantine owns visual primitives and responsive layout behavior, while TanStack
 * Query owns server state caching, request deduplication, and mutation
 * invalidation. Keeping them together makes `main.tsx` and `App.tsx` boring in
 * the best possible way.
 */
export function AppProviders({ children }: AppProvidersProps) {
  return (
    <MantineProvider theme={theme}>
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    </MantineProvider>
  )
}
