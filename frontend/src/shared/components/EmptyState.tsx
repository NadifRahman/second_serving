import { Paper, Stack, Text, Title } from '@mantine/core'
import type { ReactNode } from 'react'

type EmptyStateProps = {
  title: string
  message: string
  action?: ReactNode
}

/**
 * Reusable empty-state block for list views.
 */
export function EmptyState({ title, message, action }: EmptyStateProps) {
  return (
    <Paper withBorder p="lg" radius="sm">
      <Stack align="flex-start" gap="xs">
        <Title order={3}>{title}</Title>
        <Text c="dimmed">{message}</Text>
        {action}
      </Stack>
    </Paper>
  )
}
