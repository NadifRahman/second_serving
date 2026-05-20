import { create } from 'zustand'
import type { CurrentUser } from '../api/types'

type AuthState = {
  user: CurrentUser | null
  setUser: (user: CurrentUser | null) => void
}

/**
 * Lightweight auth mirror for synchronous UI decisions.
 *
 * TanStack Query remains the source of truth for `/api/users/me`. This store is
 * only a convenient mirror for layout components and redirects that should not
 * need to thread query results through several layers.
 */
export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
}))
