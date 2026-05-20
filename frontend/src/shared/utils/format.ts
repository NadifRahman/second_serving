/**
 * Formats ISO date strings for compact UI display.
 */
export function formatDateTime(value: string | undefined) {
  if (!value) {
    return 'Not set'
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

/**
 * Converts meters into a human-friendly distance label.
 */
export function formatMeters(value: number) {
  if (value >= 1000) {
    return `${Math.round(value / 1000)} km`
  }

  return `${Math.round(value)} m`
}

/**
 * Calculates approximate distance between two coordinates in meters.
 */
export function distanceInMeters(
  first: { latitude: number; longitude: number },
  second: { latitude: number; longitude: number },
) {
  const earthRadiusMeters = 6_371_000
  const firstLatitude = (first.latitude * Math.PI) / 180
  const secondLatitude = (second.latitude * Math.PI) / 180
  const latitudeDelta = ((second.latitude - first.latitude) * Math.PI) / 180
  const longitudeDelta = ((second.longitude - first.longitude) * Math.PI) / 180

  const haversine =
    Math.sin(latitudeDelta / 2) ** 2 +
    Math.cos(firstLatitude) *
      Math.cos(secondLatitude) *
      Math.sin(longitudeDelta / 2) ** 2

  return (
    2 *
    earthRadiusMeters *
    Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
  )
}

/**
 * Converts an ISO instant into the value shape expected by `datetime-local`.
 */
export function inputDateTimeFromIso(value: string | undefined) {
  if (!value) {
    return ''
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
    .toISOString()
    .slice(0, 16)
}

/**
 * Converts a `datetime-local` value back into an ISO instant for the API.
 */
export function isoFromInputDateTime(value: string) {
  return new Date(value).toISOString()
}
