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

export function RegisterForm({
  className,
  ...props
}: React.ComponentProps<'div'>) {
  const navigate = useNavigate()
  const [error, formAction] = useActionState(
    async (_prevState: string | null, formData: FormData) => {
      const pseudo = formData.get('pseudo')?.toString().trim() ?? ''
      const email = formData.get('email')?.toString().trim() ?? ''
      const password = formData.get('password')?.toString() ?? ''

      if (!pseudo || !email || !password) {
        return 'All fields are required.'
      }

      if (password.length < 8) {
        return 'Password must be at least 8 characters long.'
      }

      try {
        const res = await apiFetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ pseudo, email, password }),
        })

        if (!res.ok) {
          const data = await res.json().catch(() => null)
          return data?.message ?? 'Registration failed. Please try again.'
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
          <CardTitle className="text-xl">Create an account</CardTitle>
          <CardDescription>
            Enter your details below to get started
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form action={formAction}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="pseudo">Username</FieldLabel>
                <Input
                  id="pseudo"
                  name="pseudo"
                  type="text"
                  placeholder="Your username"
                  autoComplete="username"
                  required
                />
              </Field>
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
              <Field>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="At least 8 characters"
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
                  label="Create account"
                  pendingLabel="Creating account..."
                />
                <FieldDescription className="text-center">
                  Already have an account? <Link to="/login">Sign in</Link>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
