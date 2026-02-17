import { authQueryOptions, useAuth } from '@/hooks/use-auth'
import {
  createFileRoute,
  Outlet,
  redirect,
  useNavigate,
} from '@tanstack/react-router'
import { useEffect } from 'react'

export const Route = createFileRoute('/_app/_protected')({
  beforeLoad: async ({ context, location }) => {
    // During SSR, browser cookies aren't available for API calls.
    // The component handles the fallback redirect after hydration.
    if (typeof window === 'undefined') return

    const user = await context.queryClient.ensureQueryData(authQueryOptions)

    if (!user) {
      throw redirect({
        to: '/login',
        search: { redirect: location.pathname },
      })
    }
  },
  component: ProtectedLayout,
})

function ProtectedLayout() {
  const { user, isLoading } = useAuth()
  const navigate = useNavigate()

  // Fallback redirect for SSR-hydrated pages where beforeLoad didn't run
  useEffect(() => {
    if (!isLoading && !user) {
      void navigate({ to: '/login', search: { redirect: location.pathname } })
    }
  }, [isLoading, user, navigate])

  if (isLoading || !user) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  return <Outlet />
}
