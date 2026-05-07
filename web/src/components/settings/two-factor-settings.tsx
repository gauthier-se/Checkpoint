import { useState } from 'react'
import { useForm } from '@tanstack/react-form'
import { useQueryClient } from '@tanstack/react-query'
import { ShieldCheck, ShieldOff } from 'lucide-react'
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
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { authQueryOptions, useAuth } from '@/hooks/use-auth'
import { apiFetch } from '@/services/api'

type SetupState = 'idle' | 'pending-qr' | 'pending-verify'

const verifySchema = z.object({
  code: z.string().length(6, 'Code must be exactly 6 digits'),
})

const disableSchema = z.object({
  password: z.string().min(1, 'Password is required'),
  code: z.string().length(6, 'Code must be exactly 6 digits'),
})

export function TwoFactorSettings() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [setupState, setSetupState] = useState<SetupState>('idle')
  const [qrCodeDataUrl, setQrCodeDataUrl] = useState<string | null>(null)
  const [provisioningUri, setProvisioningUri] = useState<string | null>(null)

  const verifyForm = useForm({
    defaultValues: { code: '' },
    validators: { onSubmit: verifySchema },
    onSubmit: async ({ value }) => {
      const res = await apiFetch('/api/auth/2fa/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(value),
      })
      if (!res.ok) {
        const data = await res.json().catch(() => null)
        toast.error(data?.message ?? 'Invalid code. Please try again.')
        return
      }
      toast.success('Two-factor authentication enabled.')
      setSetupState('idle')
      setQrCodeDataUrl(null)
      await queryClient.invalidateQueries({
        queryKey: authQueryOptions.queryKey,
      })
    },
  })

  const disableForm = useForm({
    defaultValues: { password: '', code: '' },
    validators: { onSubmit: disableSchema },
    onSubmit: async ({ value }) => {
      const res = await apiFetch('/api/auth/2fa/disable', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(value),
      })
      if (!res.ok) {
        const data = await res.json().catch(() => null)
        toast.error(data?.message ?? 'Failed to disable 2FA.')
        return
      }
      toast.success('Two-factor authentication disabled.')
      await queryClient.invalidateQueries({
        queryKey: authQueryOptions.queryKey,
      })
    },
  })

  const handleSetupStart = async () => {
    const res = await apiFetch('/api/auth/2fa/setup', { method: 'POST' })
    if (!res.ok) {
      toast.error('Failed to start 2FA setup.')
      return
    }
    const data = await res.json()
    setQrCodeDataUrl(data.qrCodeDataUrl)
    setProvisioningUri(data.provisioningUri)
    setSetupState('pending-qr')
  }

  if (!user) return null

  if (user.twoFactorEnabled) {
    return (
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <ShieldCheck className="text-green-500 size-5" />
            <CardTitle>Two-Factor Authentication</CardTitle>
          </div>
          <CardDescription>
            Your account is protected with TOTP-based 2FA. You will be asked for
            a code from your authenticator app at every login.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={(e) => {
              e.preventDefault()
              e.stopPropagation()
              disableForm.handleSubmit()
            }}
            className="space-y-4"
          >
            <disableForm.Field
              name="password"
              children={(field) => (
                <Field>
                  <FieldLabel htmlFor="disable-password">
                    Current password
                  </FieldLabel>
                  <Input
                    id="disable-password"
                    type="password"
                    autoComplete="current-password"
                    value={field.state.value}
                    onBlur={field.handleBlur}
                    onChange={(e) => field.handleChange(e.target.value)}
                  />
                  {field.state.meta.errors.length > 0 && (
                    <p className="text-sm text-destructive">
                      {field.state.meta.errors
                        .map((e) =>
                          typeof e === 'string' ? e : (e as any).message,
                        )
                        .join(', ')}
                    </p>
                  )}
                </Field>
              )}
            />
            <disableForm.Field
              name="code"
              children={(field) => (
                <Field>
                  <FieldLabel htmlFor="disable-code">
                    Authenticator code
                  </FieldLabel>
                  <Input
                    id="disable-code"
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    placeholder="000000"
                    autoComplete="one-time-code"
                    value={field.state.value}
                    onBlur={field.handleBlur}
                    onChange={(e) => field.handleChange(e.target.value)}
                  />
                  {field.state.meta.errors.length > 0 && (
                    <p className="text-sm text-destructive">
                      {field.state.meta.errors
                        .map((e) =>
                          typeof e === 'string' ? e : (e as any).message,
                        )
                        .join(', ')}
                    </p>
                  )}
                </Field>
              )}
            />
            <disableForm.Subscribe
              selector={(state) => [state.canSubmit, state.isSubmitting]}
              children={([canSubmit, isSubmitting]) => (
                <Button
                  type="submit"
                  variant="destructive"
                  disabled={!canSubmit || isSubmitting}
                >
                  <ShieldOff className="mr-2 size-4" />
                  {isSubmitting ? 'Disabling...' : 'Disable 2FA'}
                </Button>
              )}
            />
          </form>
        </CardContent>
      </Card>
    )
  }

  if (setupState === 'pending-qr' && qrCodeDataUrl) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Scan QR Code</CardTitle>
          <CardDescription>
            Scan this QR code with your authenticator app (Google Authenticator,
            Authy, etc.), then enter the 6-digit code to confirm.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex flex-col items-center gap-4">
            <img
              src={qrCodeDataUrl}
              alt="TOTP QR Code"
              className="rounded-md border"
              width={200}
              height={200}
            />
            {provisioningUri && (
              <p className="text-muted-foreground max-w-xs break-all text-center text-xs">
                Can&apos;t scan? Enter this code manually:{' '}
                <span className="font-mono font-semibold">
                  {new URL(provisioningUri).searchParams.get('secret')}
                </span>
              </p>
            )}
          </div>
          <form
            onSubmit={(e) => {
              e.preventDefault()
              e.stopPropagation()
              verifyForm.handleSubmit()
            }}
            className="space-y-4"
          >
            <verifyForm.Field
              name="code"
              children={(field) => (
                <Field>
                  <FieldLabel htmlFor="verify-code">
                    6-digit confirmation code
                  </FieldLabel>
                  <Input
                    id="verify-code"
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    placeholder="000000"
                    autoComplete="one-time-code"
                    value={field.state.value}
                    onBlur={field.handleBlur}
                    onChange={(e) => field.handleChange(e.target.value)}
                  />
                  {field.state.meta.errors.length > 0 && (
                    <p className="text-sm text-destructive">
                      {field.state.meta.errors
                        .map((e) =>
                          typeof e === 'string' ? e : (e as any).message,
                        )
                        .join(', ')}
                    </p>
                  )}
                </Field>
              )}
            />
            <div className="flex gap-2">
              <verifyForm.Subscribe
                selector={(state) => [state.canSubmit, state.isSubmitting]}
                children={([canSubmit, isSubmitting]) => (
                  <Button type="submit" disabled={!canSubmit || isSubmitting}>
                    {isSubmitting ? 'Verifying...' : 'Confirm & Enable 2FA'}
                  </Button>
                )}
              />
              <Button
                type="button"
                variant="outline"
                onClick={() => setSetupState('idle')}
              >
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <ShieldOff className="text-muted-foreground size-5" />
          <CardTitle>Two-Factor Authentication</CardTitle>
        </div>
        <CardDescription>
          Add an extra layer of security to your account. Once enabled, you will
          need a code from your authenticator app to sign in.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Button onClick={handleSetupStart}>
          <ShieldCheck className="mr-2 size-4" />
          Enable 2FA
        </Button>
      </CardContent>
    </Card>
  )
}
