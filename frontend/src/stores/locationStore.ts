import { create } from 'zustand'
import { mapConfig } from '../config/map'

export type UserLocation = {
  latitude: number
  longitude: number
  source: 'browser' | 'fallback' | 'manual'
}

type LocationState = {
  location: UserLocation
  radiusMeters: number
  setLocation: (location: UserLocation) => void
  setRadiusMeters: (radiusMeters: number) => void
}

/**
 * Client-side map search state.
 *
 * This belongs in Zustand instead of TanStack Query because it is local UI
 * preference state. Query uses these values as inputs, but the server does not
 * own the user's current map center or selected radius.
 */
export const useLocationStore = create<LocationState>((set) => ({
  location: {
    ...mapConfig.defaultCenter,
    source: 'fallback',
  },
  radiusMeters: mapConfig.defaultRadiusMeters,
  setLocation: (location) => set({ location }),
  setRadiusMeters: (radiusMeters) => set({ radiusMeters }),
}))
