import { Link, useNavigate } from '@tanstack/react-router'
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

interface ResetPasswordFormProps extends React.ComponentProps<'div'> {
  token: string
}

export function ResetPasswordForm({
  className,
  token,
  ...props
}: ResetPasswordFormProps) {
  const navigate = useNavigate()
  const [error, formAction] = useActionState(
    async (_prevState: string | null, formData: FormData) => {
      const newPassword = formData.get('newPassword')?.toString() ?? ''
      const confirmPassword = formData.get('confirmPassword')?.toString() ?? ''

      if (!newPassword || !confirmPassword) {
        return 'Both fields are required.'
      }

      if (newPassword.length < 8) {
        return 'Password must be at least 8 characters long.'
      }

      if (newPassword !== confirmPassword) {
        return 'Passwords do not match.'
      }

      try {
        const res = await apiFetch('/api/auth/reset-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token, newPassword }),
        })

        if (!res.ok) {
          const data = await res.json().catch(() => null)
          return data?.message ?? 'Password reset failed. Please try again.'
        }

        await navigate({ to: '/login' })
        return null
      } catch {
        return 'Unable to reach the server. Please try again later.'
      }
    },
    null,
  )

  return (
    <div className={cn('flex flex-col gap-6', className)} {...props}>
      <Card>
        <CardHeader className="text-center">
          <CardTitle className="text-xl">Reset your password</CardTitle>
          <CardDescription>Enter your new password below</CardDescription>
        </CardHeader>
        <CardContent>
          <form action={formAction}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="newPassword">New password</FieldLabel>
                <Input
                  id="newPassword"
                  name="newPassword"
                  type="password"
                  placeholder="At least 8 characters"
                  autoComplete="new-password"
                  minLength={8}
                  required
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="confirmPassword">
                  Confirm password
                </FieldLabel>
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  placeholder="Repeat your password"
                  autoComplete="new-password"
                  minLength={8}
                  required
                />
              </Field>
              {error && (
                <p className="text-sm text-destructive text-center">{error}</p>
              )}
              <Field>
                <SubmitButton
                  label="Reset password"
                  pendingLabel="Resetting..."
                />
                <FieldDescription className="text-center">
                  <Link to="/login">Back to login</Link>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
