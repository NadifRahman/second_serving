import { zodResolver } from '@hookform/resolvers/zod'
import { Alert, Button, Paper, PasswordInput, Stack, Text, TextInput, Title } from '@mantine/core'
import { Controller, useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { routes } from '../../config/routes'
import { useAuthMutations } from './hooks'
import { signUpSchema, type SignUpFormValues } from './schemas'

/**
 * Public sign-up page.
 */
export function SignUpPage() {
  const navigate = useNavigate()
  const auth = useAuthMutations()
  const form = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      username: '',
      password: '',
      fullName: '',
      email: '',
    },
  })

  const errorMessage =
    auth.signup.error instanceof ApiError
      ? auth.signup.error.message
      : 'Unable to create your account right now.'

  const handleSubmit = async (values: SignUpFormValues) => {
    await auth.signup.mutateAsync(values)
    navigate(routes.home)
  }

  return (
    <Stack maw={520} mx="auto" py="xl">
      <Stack gap={4}>
        <Title order={1}>Sign up</Title>
        <Text c="dimmed">Create an account to post listings and reserve food.</Text>
      </Stack>

      <Paper withBorder p="lg" radius="sm">
        <form onSubmit={form.handleSubmit(handleSubmit)}>
          <Stack>
            {auth.signup.isError ? <Alert color="red">{errorMessage}</Alert> : null}
            <Controller
              control={form.control}
              name="fullName"
              render={({ field, fieldState }) => (
                <TextInput
                  required
                  error={fieldState.error?.message}
                  label="Full name"
                  {...field}
                />
              )}
            />
            <Controller
              control={form.control}
              name="email"
              render={({ field, fieldState }) => (
                <TextInput
                  required
                  error={fieldState.error?.message}
                  label="Email"
                  type="email"
                  {...field}
                />
              )}
            />
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
            <Button loading={auth.signup.isPending} type="submit">
              Sign up
            </Button>
          </Stack>
        </form>
      </Paper>

      <Text c="dimmed" size="sm" ta="center">
        Already have an account? <Link to={routes.signIn}>Sign in</Link>
      </Text>
    </Stack>
  )
}
