import { Center, Loader } from '@mantine/core'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { routes } from '../../config/routes'
import { useCurrentUser } from '../../features/auth/hooks'

/**
 * Guards routes that require an authenticated cookie session.
 */
export function ProtectedRoute() {
  const location = useLocation()
  const currentUser = useCurrentUser()

  if (currentUser.isLoading) {
    return (
      <Center mih={300}>
        <Loader />
      </Center>
    )
  }

  if (currentUser.isError) {
    return <Navigate replace state={{ from: location }} to={routes.signIn} />
  }

  return <Outlet />
}
