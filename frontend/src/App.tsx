import { RouterProvider } from 'react-router-dom'
import { AppProviders } from './app/providers'
import { router } from './app/router'

/**
 * Root application component.
 *
 * The root stays intentionally small so global concerns are easy to locate:
 * providers live in `app/providers`, route declarations live in `app/router`,
 * and feature behavior lives under `features/*`.
 */
function App() {
  return (
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  )
}

export default App
