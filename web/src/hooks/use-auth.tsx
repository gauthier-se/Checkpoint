import { queryOptions, useQuery, useQueryClient } from '@tanstack/react-query'
import { useRouter } from '@tanstack/react-router'
import type { User } from '@/types/user'
import { apiFetch } from '@/services/api'

export const authQueryOptions = queryOptions({
  queryKey: ['auth', 'me'],
  queryFn: async (): Promise<User | null> => {
    const res = await apiFetch('/api/auth/me')
    if (!res.ok) return null
    return res.json()
  },
  staleTime: 5 * 60_000,
  retry: false,
})

export function useAuth() {
  const queryClient = useQueryClient()
  const router = useRouter()

  const { data: user = null, isPending } = useQuery({
    ...authQueryOptions,
    // During SSR, browser cookies aren't available — skip the fetch.
    // The query will run on the client after hydration.
    enabled: typeof window !== 'undefined',
  })

  const logout = async () => {
    try {
      await apiFetch('/api/auth/logout', { method: 'POST' })
    } finally {
      queryClient.setQueryData(authQueryOptions.queryKey, null)
      await router.invalidate()
    }
  }

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: authQueryOptions.queryKey })

  return { user, isLoading: isPending, logout, invalidate } as const
}
