import { useForm } from '@tanstack/react-form'
import { Link, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
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

  const resetPasswordSchema = z
    .object({
      newPassword: z
        .string()
        .min(8, 'Password must be at least 8 characters long'),
      confirmPassword: z.string().min(1, 'Password confirmation is required'),
    })
    .refine((data) => data.newPassword === data.confirmPassword, {
      message: 'Passwords do not match',
      path: ['confirmPassword'],
    })

  const form = useForm({
    defaultValues: {
      newPassword: '',
      confirmPassword: '',
    },
    validators: {
      onChange: resetPasswordSchema,
    },
    onSubmit: async ({ value }) => {
      const res = await apiFetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword: value.newPassword }),
      })

      if (!res.ok) {
        const data = await res.json().catch(() => null)
        toast.error(data?.message ?? 'Password reset failed. Please try again.')
        return
      }

      await navigate({ to: '/login' })
    },
  })

  return (
    <div className={cn('flex flex-col gap-6', className)} {...props}>
      <Card>
        <CardHeader className="text-center">
          <CardTitle className="text-xl">Reset your password</CardTitle>
          <CardDescription>Enter your new password below</CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={(e) => {
              e.preventDefault()
              e.stopPropagation()
              form.handleSubmit()
            }}
          >
            <FieldGroup>
              <form.Field
                name="newPassword"
                children={(field) => (
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
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={(e) => field.handleChange(e.target.value)}
                    />
                    {field.state.meta.errors.length > 0 && (
                      <p className="text-sm text-destructive">
                        {field.state.meta.errors.join(', ')}
                      </p>
                    )}
                  </Field>
                )}
              />
              <form.Field
                name="confirmPassword"
                children={(field) => (
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
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={(e) => field.handleChange(e.target.value)}
                    />
                    {field.state.meta.errors.length > 0 && (
                      <p className="text-sm text-destructive">
                        {field.state.meta.errors.join(', ')}
                      </p>
                    )}
                  </Field>
                )}
              />

              <Field>
                <form.Subscribe
                  selector={(state) => [state.canSubmit, state.isSubmitting]}
                  children={([canSubmit, isSubmitting]) => (
                    <Button type="submit" disabled={!canSubmit || isSubmitting}>
                      {isSubmitting ? 'Resetting...' : 'Reset password'}
                    </Button>
                  )}
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
