import { zodResolver } from '@hookform/resolvers/zod'
import {
  Alert,
  Button,
  Grid,
  NumberInput,
  Paper,
  Select,
  Stack,
  Textarea,
  TextInput,
  Title,
} from '@mantine/core'
import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { FoodListing } from '../../api/types'
import { routes } from '../../config/routes'
import { mapConfig } from '../../config/map'
import {
  inputDateTimeFromIso,
  isoFromInputDateTime,
} from '../../shared/utils/format'
import { FoodMap } from '../map/FoodMap'
import { useListing, useListingMutations } from './hooks'
import {
  listingFormSchema,
  listingStatusOptions,
  quantityUnitOptions,
  type ListingFormValues,
} from './schemas'

type ListingFormPageProps = {
  mode: 'create' | 'edit'
}

const defaultCreateDates = () => {
  const now = new Date()
  const pickupStart = new Date(now)
  pickupStart.setMinutes(0, 0, 0)
  pickupStart.setHours(pickupStart.getHours() + 1)

  const pickupEnd = new Date(pickupStart)
  pickupEnd.setHours(pickupEnd.getHours() + 2)

  return {
    expiresAt: inputDateTimeFromIso(pickupEnd.toISOString()),
    pickupStartAt: inputDateTimeFromIso(pickupStart.toISOString()),
    pickupEndAt: inputDateTimeFromIso(pickupEnd.toISOString()),
  }
}

const createDefaultListingValues = (): ListingFormValues => {
  const dates = defaultCreateDates()

  return {
    title: '',
    description: '',
    status: 'AVAILABLE',
    quantity: 1,
    quantityUnit: 'ITEM',
    expiresAt: dates.expiresAt,
    pickupLocation: {
      fullAddress: '',
      latitude: mapConfig.defaultCenter.latitude,
      longitude: mapConfig.defaultCenter.longitude,
      pickupStartAt: dates.pickupStartAt,
      pickupEndAt: dates.pickupEndAt,
      instructions: '',
    },
  }
}

const valuesFromListing = (listing: FoodListing): ListingFormValues => {
  const fallback = createDefaultListingValues()

  return {
    title: listing.title ?? fallback.title,
    description: listing.description ?? fallback.description,
    status: listing.status ?? fallback.status,
    quantity: listing.quantity ?? fallback.quantity,
    quantityUnit: listing.quantityUnit ?? fallback.quantityUnit,
    expiresAt: inputDateTimeFromIso(listing.expiresAt) || fallback.expiresAt,
    pickupLocation: {
      fullAddress:
        listing.pickupLocation?.fullAddress ?? fallback.pickupLocation.fullAddress,
      latitude: listing.pickupLocation?.latitude ?? fallback.pickupLocation.latitude,
      longitude:
        listing.pickupLocation?.longitude ?? fallback.pickupLocation.longitude,
      pickupStartAt:
        inputDateTimeFromIso(listing.pickupLocation?.pickupStartAt) ||
        fallback.pickupLocation.pickupStartAt,
      pickupEndAt:
        inputDateTimeFromIso(listing.pickupLocation?.pickupEndAt) ||
        fallback.pickupLocation.pickupEndAt,
      instructions:
        listing.pickupLocation?.instructions ?? fallback.pickupLocation.instructions,
    },
  }
}

/**
 * Shared create/edit listing page.
 *
 * Edit mode intentionally submits only the backend's minimal patch fields:
 * status, quantity, and expiration. Create mode submits the full listing and
 * pickup location payload required by Spring.
 */
export function ListingFormPage({ mode }: ListingFormPageProps) {
  const { listingId } = useParams()
  const navigate = useNavigate()
  const mutations = useListingMutations()
  const listing = useListing(mode === 'edit' ? listingId : undefined)
  const existing = listing.data
  const submitError = mutations.create.error ?? mutations.update.error
  const submitErrorMessage =
    submitError instanceof ApiError
      ? submitError.responseBody
      : 'Could not save this listing.'

  const form = useForm<ListingFormValues>({
    resolver: zodResolver(listingFormSchema),
    defaultValues: createDefaultListingValues(),
  })

  const watchedLocation = form.watch('pickupLocation')

  useEffect(() => {
    if (mode === 'edit' && existing) {
      form.reset(valuesFromListing(existing))
    }
  }, [existing, form, mode])

  const handleSubmit = async (values: ListingFormValues) => {
    try {
      if (mode === 'edit' && listingId) {
        const updated = await mutations.update.mutateAsync({
          listingId,
          request: {
            status: values.status,
            quantity: values.quantity,
            expiresAt: isoFromInputDateTime(values.expiresAt),
          },
        })
        navigate(routes.listingDetail(updated.listingId ?? listingId))
        return
      }

      const created = await mutations.create.mutateAsync({
        ...values,
        expiresAt: isoFromInputDateTime(values.expiresAt),
        pickupLocation: {
          ...values.pickupLocation,
          pickupStartAt: isoFromInputDateTime(values.pickupLocation.pickupStartAt),
          pickupEndAt: isoFromInputDateTime(values.pickupLocation.pickupEndAt),
        },
      })
      navigate(created.listingId ? routes.listingDetail(created.listingId) : routes.home)
    } catch {
      // Mutation state renders the backend's string error above the form.
    }
  }

  return (
    <Stack gap="md" py="md">
      <Title order={1}>{mode === 'create' ? 'New listing' : 'Edit listing'}</Title>
      <form onSubmit={form.handleSubmit(handleSubmit)}>
        {submitError ? (
          <Alert color="red" mb="md">
            {submitErrorMessage}
          </Alert>
        ) : null}
        <Grid>
          <Grid.Col span={{ base: 12, md: 7 }}>
            <Paper withBorder p="md" radius="sm">
              <Stack>
                <Controller
                  control={form.control}
                  name="title"
                  render={({ field, fieldState }) => (
                    <TextInput
                      disabled={mode === 'edit'}
                      error={fieldState.error?.message}
                      label="Title"
                      required
                      {...field}
                    />
                  )}
                />
                <Controller
                  control={form.control}
                  name="description"
                  render={({ field, fieldState }) => (
                    <Textarea
                      disabled={mode === 'edit'}
                      error={fieldState.error?.message}
                      label="Description"
                      minRows={4}
                      {...field}
                    />
                  )}
                />
                <Grid>
                  <Grid.Col span={{ base: 12, sm: 4 }}>
                    <Controller
                      control={form.control}
                      name="status"
                      render={({ field, fieldState }) => (
                        <Select
                          data={listingStatusOptions}
                          error={fieldState.error?.message}
                          label="Status"
                          required
                          {...field}
                        />
                      )}
                    />
                  </Grid.Col>
                  <Grid.Col span={{ base: 12, sm: 4 }}>
                    <Controller
                      control={form.control}
                      name="quantity"
                      render={({ field, fieldState }) => (
                        <NumberInput
                          error={fieldState.error?.message}
                          label="Quantity"
                          min={1}
                          required
                          value={field.value}
                          onChange={field.onChange}
                        />
                      )}
                    />
                  </Grid.Col>
                  <Grid.Col span={{ base: 12, sm: 4 }}>
                    <Controller
                      control={form.control}
                      name="quantityUnit"
                      render={({ field, fieldState }) => (
                        <Select
                          data={quantityUnitOptions}
                          disabled={mode === 'edit'}
                          error={fieldState.error?.message}
                          label="Unit"
                          required
                          {...field}
                        />
                      )}
                    />
                  </Grid.Col>
                </Grid>
                <Controller
                  control={form.control}
                  name="expiresAt"
                  render={({ field, fieldState }) => (
                    <TextInput
                      error={fieldState.error?.message}
                      label="Expires at"
                      required
                      type="datetime-local"
                      {...field}
                    />
                  )}
                />
              </Stack>
            </Paper>
          </Grid.Col>

          <Grid.Col span={{ base: 12, md: 5 }}>
            <Stack>
              <Paper withBorder p="md" radius="sm">
                <Stack>
                  <Controller
                    control={form.control}
                    name="pickupLocation.fullAddress"
                    render={({ field, fieldState }) => (
                      <TextInput
                        disabled={mode === 'edit'}
                        error={fieldState.error?.message}
                        label="Pickup address"
                        required
                        {...field}
                      />
                    )}
                  />
                  <Grid>
                    <Grid.Col span={6}>
                      <Controller
                        control={form.control}
                        name="pickupLocation.latitude"
                        render={({ field, fieldState }) => (
                          <NumberInput
                            disabled={mode === 'edit'}
                            error={fieldState.error?.message}
                            label="Latitude"
                            max={90}
                            min={-90}
                            required
                            value={field.value}
                            onChange={field.onChange}
                          />
                        )}
                      />
                    </Grid.Col>
                    <Grid.Col span={6}>
                      <Controller
                        control={form.control}
                        name="pickupLocation.longitude"
                        render={({ field, fieldState }) => (
                          <NumberInput
                            disabled={mode === 'edit'}
                            error={fieldState.error?.message}
                            label="Longitude"
                            max={180}
                            min={-180}
                            required
                            value={field.value}
                            onChange={field.onChange}
                          />
                        )}
                      />
                    </Grid.Col>
                  </Grid>
                  <Controller
                    control={form.control}
                    name="pickupLocation.pickupStartAt"
                    render={({ field, fieldState }) => (
                      <TextInput
                        disabled={mode === 'edit'}
                        error={fieldState.error?.message}
                        label="Pickup starts"
                        required
                        type="datetime-local"
                        {...field}
                      />
                    )}
                  />
                  <Controller
                    control={form.control}
                    name="pickupLocation.pickupEndAt"
                    render={({ field, fieldState }) => (
                      <TextInput
                        disabled={mode === 'edit'}
                        error={fieldState.error?.message}
                        label="Pickup ends"
                        required
                        type="datetime-local"
                        {...field}
                      />
                    )}
                  />
                  <Controller
                    control={form.control}
                    name="pickupLocation.instructions"
                    render={({ field, fieldState }) => (
                      <Textarea
                        disabled={mode === 'edit'}
                        error={fieldState.error?.message}
                        label="Instructions"
                        {...field}
                      />
                    )}
                  />
                </Stack>
              </Paper>
              <FoodMap
                draggableMarker={
                  mode === 'create'
                    ? {
                        latitude: watchedLocation.latitude,
                        longitude: watchedLocation.longitude,
                        label: 'Drag to set the pickup location',
                        onDragEnd: (latitude, longitude) => {
                          form.setValue('pickupLocation.latitude', latitude, {
                            shouldDirty: true,
                            shouldValidate: true,
                          })
                          form.setValue('pickupLocation.longitude', longitude, {
                            shouldDirty: true,
                            shouldValidate: true,
                          })
                        },
                      }
                    : undefined
                }
                height={300}
                initialLatitude={watchedLocation.latitude}
                initialLongitude={watchedLocation.longitude}
                initialZoom={mapConfig.detailZoom}
                listings={mode === 'edit' && existing ? [existing] : []}
              />
            </Stack>
          </Grid.Col>
        </Grid>

        <Button
          loading={mutations.create.isPending || mutations.update.isPending}
          mt="md"
          type="submit"
        >
          {mode === 'create' ? 'Create listing' : 'Save changes'}
        </Button>
      </form>
    </Stack>
  )
}
