import { zodResolver } from '@hookform/resolvers/zod'
import { Alert, Button, Paper, PasswordInput, Stack, Text, TextInput, Title } from '@mantine/core'
import { Controller, useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { routes } from '../../config/routes'
import { useAuthMutations } from './hooks'
import { signInSchema, type SignInFormValues } from './schemas'

/**
 * Public sign-in page.
 */
export function SignInPage() {
  const navigate = useNavigate()
  const auth = useAuthMutations()
  const form = useForm<SignInFormValues>({
    resolver: zodResolver(signInSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const errorMessage =
    auth.login.error instanceof ApiError
      ? auth.login.error.message
      : 'Unable to sign in right now.'

  const handleSubmit = async (values: SignInFormValues) => {
    await auth.login.mutateAsync(values)
    navigate(routes.home)
  }

  return (
    <Stack maw={440} mx="auto" py="xl">
      <Stack gap={4}>
        <Title order={1}>Sign in</Title>
        <Text c="dimmed">Access your listings and reservations.</Text>
      </Stack>

      <Paper withBorder p="lg" radius="sm">
        <form onSubmit={form.handleSubmit(handleSubmit)}>
          <Stack>
            {auth.login.isError ? <Alert color="red">{errorMessage}</Alert> : null}
            <Controller
              control={form.control}
              name="username"
              render={({ field, fieldState }) => (
                <TextInput
                  required
                  error={fieldState.error?.message}
                  label="Username"
                  {...field}
                />
              )}
            />
            <Controller
              control={form.control}
              name="password"
              render={({ field, fieldState }) => (
                <PasswordInput
                  required
                  error={fieldState.error?.message}
                  label="Password"
                  {...field}
                />
              )}
            />
            <Button loading={auth.login.isPending} type="submit">
              Sign in
            </Button>
          </Stack>
        </form>
      </Paper>

      <Text c="dimmed" size="sm" ta="center">
        Need an account? <Link to={routes.signUp}>Sign up</Link>
      </Text>
    </Stack>
  )
}
