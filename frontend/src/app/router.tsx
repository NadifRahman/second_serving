import { createBrowserRouter } from 'react-router-dom'
import { SignInPage } from '../features/auth/SignInPage'
import { SignUpPage } from '../features/auth/SignUpPage'
import { ListingDetailPage } from '../features/listings/ListingDetailPage'
import { ListingFormPage } from '../features/listings/ListingFormPage'
import { ListingsMapPage } from '../features/listings/ListingsMapPage'
import { MyListingsPage } from '../features/listings/MyListingsPage'
import { MyReservationsPage } from '../features/reservations/MyReservationsPage'
import { routes } from '../config/routes'
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
      { path: routes.home, element: <ListingsMapPage /> },
      { path: routes.signIn, element: <SignInPage /> },
      { path: routes.signUp, element: <SignUpPage /> },
      { path: routes.listingDetail(':listingId'), element: <ListingDetailPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: routes.newListing, element: <ListingFormPage mode="create" /> },
          {
            path: routes.editListing(':listingId'),
            element: <ListingFormPage mode="edit" />,
          },
          { path: routes.myListings, element: <MyListingsPage /> },
          { path: routes.myReservations, element: <MyReservationsPage /> },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
