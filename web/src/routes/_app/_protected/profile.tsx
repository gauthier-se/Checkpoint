import { createFileRoute, redirect } from '@tanstack/react-router'
import { authQueryOptions } from '@/hooks/use-auth'

export const Route = createFileRoute('/_app/_protected/profile')({
  beforeLoad: async ({ context }) => {
    const user = await context.queryClient.ensureQueryData(authQueryOptions)
    if (user) {
      throw redirect({
        to: '/profile/$username',
        params: { username: user.username },
      })
    }
  },
  component: () => null,
})
