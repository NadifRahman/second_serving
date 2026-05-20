import { z } from 'zod'

/**
 * Login form validation.
 *
 * The backend owns authentication rules, but the frontend still validates empty
 * fields so users get immediate feedback before a request is sent.
 */
export const signInSchema = z.object({
  username: z.string().trim().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
})

/**
 * Signup form validation.
 *
 * These constraints are intentionally ordinary and user-facing. If backend DTO
 * constraints become stricter, this schema should be updated beside the
 * regenerated OpenAPI types.
 */
export const signUpSchema = z.object({
  username: z.string().trim().min(3, 'Use at least 3 characters'),
  password: z.string().min(8, 'Use at least 8 characters'),
  fullName: z.string().trim().min(1, 'Full name is required'),
  email: z.string().trim().email('Enter a valid email address'),
})

export type SignInFormValues = z.infer<typeof signInSchema>
export type SignUpFormValues = z.infer<typeof signUpSchema>
