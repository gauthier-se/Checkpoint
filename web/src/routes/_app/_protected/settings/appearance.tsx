import { createFileRoute } from '@tanstack/react-router'
import { AppearanceSettings } from '@/components/settings/appearance-settings'

export const Route = createFileRoute('/_app/_protected/settings/appearance')({
  component: SettingsAppearancePage,
})

function SettingsAppearancePage() {
  return <AppearanceSettings />
}
