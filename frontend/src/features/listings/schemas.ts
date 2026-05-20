import { z } from 'zod'
import type { ListingStatus, QuantityUnit } from '../../api/types'

export const listingStatuses = ['AVAILABLE', 'FINISHED', 'CANCELLED'] as const
export const quantityUnits = [
  'ITEM',
  'PORTION',
  'SERVING',
  'GRAM',
  'KILOGRAM',
  'MILLILITER',
  'LITER',
  'DOZEN',
  'PACKAGE',
] as const

/**
 * Validates listing creation and the shared listing form.
 *
 * Create validates all required listing and pickup fields. Edit mode reuses the
 * same component but submits only the intentionally small patch fields.
 */
export const listingFormSchema = z
  .object({
    title: z.string().trim().min(3, 'Use at least 3 characters'),
    description: z.string().trim().optional(),
    status: z.enum(listingStatuses),
    quantity: z.number().int().min(1, 'Quantity must be at least 1'),
    quantityUnit: z.enum(quantityUnits),
    expiresAt: z.string().min(1, 'Expiration is required'),
    pickupLocation: z.object({
      fullAddress: z.string().trim().min(5, 'Address is required'),
      longitude: z.number().min(-180).max(180),
      latitude: z.number().min(-90).max(90),
      pickupStartAt: z.string().min(1, 'Pickup start is required'),
      pickupEndAt: z.string().min(1, 'Pickup end is required'),
      instructions: z.string().trim().optional(),
    }),
  })
  .refine(
    (value) =>
      new Date(value.pickupLocation.pickupEndAt).getTime() >
      new Date(value.pickupLocation.pickupStartAt).getTime(),
    {
      message: 'Pickup end must be after pickup start',
      path: ['pickupLocation', 'pickupEndAt'],
    },
  )

/**
 * Validates reservation creation from a public listing page.
 */
export const reservationFormSchema = z.object({
  quantityRequested: z.number().int().min(1, 'Request at least 1'),
})

export type ListingFormValues = z.infer<typeof listingFormSchema>
export type ReservationFormValues = z.infer<typeof reservationFormSchema>

export const listingStatusOptions: { label: string; value: ListingStatus }[] =
  listingStatuses.map((status) => ({ label: status, value: status }))

export const quantityUnitOptions: { label: string; value: QuantityUnit }[] =
  quantityUnits.map((unit) => ({ label: unit, value: unit }))
