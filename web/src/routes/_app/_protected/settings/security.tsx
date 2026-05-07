import { createFileRoute } from '@tanstack/react-router'
import { TwoFactorSettings } from '@/components/settings/two-factor-settings'

export const Route = createFileRoute('/_app/_protected/settings/security')({
  component: SettingsSecurityPage,
})

function SettingsSecurityPage() {
  return <TwoFactorSettings />
}
