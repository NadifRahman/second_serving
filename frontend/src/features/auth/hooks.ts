import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect } from 'react'
import { queryKeys } from '../../api/queryKeys'
import { useAuthStore } from '../../stores/authStore'
import { getCurrentUser, login, logout, signup } from './api'

/**
 * Loads the currently authenticated user from Spring's HttpOnly cookie session.
 */
export function useCurrentUser() {
  const setUser = useAuthStore((state) => state.setUser)
  const query = useQuery({
    queryKey: queryKeys.currentUser,
    queryFn: ({ signal }) => getCurrentUser(signal),
    retry: false,
  })

  useEffect(() => {
    if (query.isSuccess) {
      setUser(query.data)
    }

    if (query.isError) {
      setUser(null)
    }
  }, [query.data, query.isError, query.isSuccess, setUser])

  return query
}

/**
 * Auth mutations that keep the user cache aligned after session changes.
 */
export function useAuthMutations() {
  const queryClient = useQueryClient()
  const setUser = useAuthStore((state) => state.setUser)

  const finishSession = async () => {
    const user = await queryClient.fetchQuery({
      queryKey: queryKeys.currentUser,
      queryFn: ({ signal }) => getCurrentUser(signal),
    })
    setUser(user)
  }

  return {
    login: useMutation({
      mutationFn: login,
      onSuccess: finishSession,
    }),
    signup: useMutation({
      mutationFn: signup,
      onSuccess: finishSession,
    }),
    logout: useMutation({
      mutationFn: logout,
      onSuccess: () => {
        setUser(null)
        queryClient.clear()
      },
    }),
  }
}
