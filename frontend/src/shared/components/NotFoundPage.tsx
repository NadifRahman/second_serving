import { Button, Stack, Text, Title } from '@mantine/core'
import { Link } from 'react-router-dom'
import { routes } from '../../config/routes'

export function NotFoundPage() {
  return (
    <Stack align="flex-start" gap="sm" py="xl">
      <Title order={1}>Page not found</Title>
      <Text c="dimmed">The page you asked for is not part of this app.</Text>
      <Button component={Link} to={routes.home}>
        Back to listings
      </Button>
    </Stack>
  )
}
