import { Link } from '@tanstack/react-router'
import { useActionState } from 'react'
import { SubmitButton } from './submit-button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { apiFetch } from '@/services/api'

type FormState = {
  error: string | null
  success: string | null
}

export function ForgotPasswordForm({
  className,
  ...props
}: React.ComponentProps<'div'>) {
  const [state, formAction] = useActionState(
    async (_prevState: FormState, formData: FormData): Promise<FormState> => {
      const email = formData.get('email')?.toString().trim() ?? ''

      if (!email) {
        return { error: 'Email is required.', success: null }
      }

      try {
        const res = await apiFetch('/api/auth/forgot-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email }),
        })

        if (!res.ok) {
          const data = await res.json().catch(() => null)
          return {
            error: data?.message ?? 'Something went wrong. Please try again.',
            success: null,
          }
        }

        return {
          error: null,
          success:
            'If an account with that email exists, a password reset link has been sent.',
        }
      } catch {
        return {
          error: 'Unable to reach the server. Please try again later.',
          success: null,
        }
      }
    },
    { error: null, success: null },
  )

  return (
    <div className={cn('flex flex-col gap-6', className)} {...props}>
      <Card>
        <CardHeader className="text-center">
          <CardTitle className="text-xl">Forgot your password?</CardTitle>
          <CardDescription>
            Enter your email and we&apos;ll send you a reset link
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form action={formAction}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="you@example.com"
                  autoComplete="email"
                  required
                />
              </Field>
              {state.error && (
                <p className="text-sm text-destructive text-center">
                  {state.error}
                </p>
              )}
              {state.success && (
                <p className="text-sm text-emerald-600 text-center">
                  {state.success}
                </p>
              )}
              <Field>
                <SubmitButton
                  label="Send reset link"
                  pendingLabel="Sending..."
                />
                <FieldDescription className="text-center">
                  Remember your password? <Link to="/login">Back to login</Link>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
