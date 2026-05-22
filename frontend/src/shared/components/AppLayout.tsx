import {
  AppShell,
  Burger,
  Button,
  Container,
  Group,
  NavLink,
  Text,
} from '@mantine/core'
import { useDisclosure } from '@mantine/hooks'
import { LogIn, LogOut, MapPinned, Plus, UserRound } from 'lucide-react'
import { NavLink as RouterNavLink, Outlet, useNavigate } from 'react-router-dom'
import secondServingLogo from '../../assets/Second_Serving_Logo.svg'
import { routes } from '../../config/routes'
import { useAuthMutations, useCurrentUser } from '../../features/auth/hooks'
import { useAuthStore } from '../../stores/authStore'

const publicLinks = [{ label: 'Explore', to: routes.home, icon: MapPinned }]
const privateLinks = [
  { label: 'New listing', to: routes.newListing, icon: Plus },
  { label: 'My listings', to: routes.myListings, icon: UserRound },
  { label: 'Reservations', to: routes.myReservations, icon: UserRound },
]

/**
 * Main application shell with responsive navigation.
 *
 * Mantine's AppShell gives us a mobile drawer, fixed header, and predictable
 * content sizing without custom layout plumbing on every page.
 */
export function AppLayout() {
  const [opened, { toggle, close }] = useDisclosure()
  const navigate = useNavigate()
  const user = useAuthStore((state) => state.user)
  const auth = useAuthMutations()

  useCurrentUser()

  const handleLogout = async () => {
    await auth.logout.mutateAsync()
    navigate(routes.home)
    close()
  }

  const links = user ? [...publicLinks, ...privateLinks] : publicLinks

  return (
    <AppShell
      header={{ height: 64 }}
      navbar={{
        width: 260,
        breakpoint: 'sm',
        collapsed: { desktop: true, mobile: !opened },
      }}
      padding="md"
    >
      <AppShell.Header>
        <Container h="100%" size="xl">
          <Group h="100%" justify="space-between">
            <Group gap="sm">
              <Burger
                hiddenFrom="sm"
                opened={opened}
                size="sm"
                onClick={toggle}
              />
              <RouterNavLink to={routes.home}>
                <img
                  alt="Second Serving"
                  src={secondServingLogo}
                  style={{ display: 'block', height: 44, width: 44 }}
                />
              </RouterNavLink>
            </Group>

            <Group gap="xs" visibleFrom="sm">
              {links.map((link) => (
                <Button
                  key={link.to}
                  component={RouterNavLink}
                  leftSection={<link.icon size={16} />}
                  to={link.to}
                  variant="subtle"
                >
                  {link.label}
                </Button>
              ))}
            </Group>

            <Group gap="xs" visibleFrom="sm">
              {user ? (
                <>
                  <Text c="dimmed" size="sm">
                    {user.username}
                  </Text>
                  <Button
                    leftSection={<LogOut size={16} />}
                    loading={auth.logout.isPending}
                    variant="light"
                    onClick={handleLogout}
                  >
                    Sign out
                  </Button>
                </>
              ) : (
                <Button
                  component={RouterNavLink}
                  leftSection={<LogIn size={16} />}
                  to={routes.signIn}
                >
                  Sign in
                </Button>
              )}
            </Group>
          </Group>
        </Container>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        {links.map((link) => (
          <NavLink
            key={link.to}
            component={RouterNavLink}
            label={link.label}
            leftSection={<link.icon size={18} />}
            to={link.to}
            onClick={close}
          />
        ))}
        {user ? (
          <NavLink
            label="Sign out"
            leftSection={<LogOut size={18} />}
            onClick={handleLogout}
          />
        ) : (
          <NavLink
            component={RouterNavLink}
            label="Sign in"
            leftSection={<LogIn size={18} />}
            to={routes.signIn}
            onClick={close}
          />
        )}
      </AppShell.Navbar>

      <AppShell.Main>
        <Container size="xl">
          <Outlet />
        </Container>
      </AppShell.Main>
    </AppShell>
  )
}
