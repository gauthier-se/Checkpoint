import { createFileRoute } from '@tanstack/react-router'
import { useSuspenseQuery } from '@tanstack/react-query'
import { EditProfileForm } from '@/components/settings/edit-profile-form'
import { useAuth } from '@/hooks/use-auth'
import { userProfileQueryOptions } from '@/queries/profile'

export const Route = createFileRoute('/_app/_protected/settings/profile')({
  loader: async ({ context }) => {
    const user = await context.queryClient.ensureQueryData({
      queryKey: ['auth', 'me'],
      queryFn: async () => {
        const { apiFetch } = await import('@/services/api')
        const res = await apiFetch('/api/auth/me')
        if (!res.ok) return null
        return res.json()
      },
    })

    if (user) {
      await context.queryClient.ensureQueryData(
        userProfileQueryOptions(user.username),
      )
    }
  },
  component: SettingsProfilePage,
})

function SettingsProfilePage() {
  const { user } = useAuth()

  if (!user) return null

  const { data: profile } = useSuspenseQuery(
    userProfileQueryOptions(user.username),
  )

  return (
    <div className="mx-auto max-w-xl py-10">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight">Settings</h1>
        <p className="text-muted-foreground text-sm">
          Manage your account settings and profile
        </p>
      </div>
      <EditProfileForm
        username={profile.username}
        bio={profile.bio}
        picture={profile.picture}
        isPrivate={profile.isPrivate}
      />
    </div>
  )
}
