import { createBrowserRouter } from 'react-router-dom'
import { SignInPage } from '../features/auth/SignInPage'
import { SignUpPage } from '../features/auth/SignUpPage'
import { ListingDetailPage } from '../features/listings/ListingDetailPage'
import { ListingFormPage } from '../features/listings/ListingFormPage'
import { ListingsMapPage } from '../features/listings/ListingsMapPage'
import { MyListingsPage } from '../features/listings/MyListingsPage'
import { MyReservationsPage } from '../features/reservations/MyReservationsPage'
import { AppLayout } from '../shared/components/AppLayout'
import { NotFoundPage } from '../shared/components/NotFoundPage'
import { ProtectedRoute } from '../shared/components/ProtectedRoute'

/**
 * SPA route table.
 *
 * Public listing routes remain visible to anonymous visitors. Owner and
 * requester workflows sit behind `ProtectedRoute`, which keeps auth decisions
 * centralized instead of scattering redirects through page components.
 */
export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [
      { path: '/', element: <ListingsMapPage /> },
      { path: '/sign-in', element: <SignInPage /> },
      { path: '/sign-up', element: <SignUpPage /> },
      { path: '/listings/:listingId', element: <ListingDetailPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: '/listings/new', element: <ListingFormPage mode="create" /> },
          {
            path: '/listings/:listingId/edit',
            element: <ListingFormPage mode="edit" />,
          },
          { path: '/me/listings', element: <MyListingsPage /> },
          { path: '/me/reservations', element: <MyReservationsPage /> },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
