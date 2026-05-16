import { MutationCache, QueryClient } from '@tanstack/react-query'
import { createRouter } from '@tanstack/react-router'
import { setupRouterSsrQueryIntegration } from '@tanstack/react-router-ssr-query'
import { toast } from 'sonner'

import { routeTree } from './routeTree.gen'
import { isApiError } from '@/services/api'

declare module '@tanstack/react-query' {
  interface Register {
    mutationMeta: {
      // Set to `true` on a useMutation to opt out of the global error toast
      // (typically because a local `onError` already shows one).
      suppressGlobalError?: boolean
    }
  }
}

export const getRouter = () => {
  const queryClient = new QueryClient({
    mutationCache: new MutationCache({
      onError: (error, _vars, _ctx, mutation) => {
        if (mutation.options.meta?.suppressGlobalError) return
        const message = isApiError(error)
          ? error.message
          : 'Something went wrong. Please try again.'
        toast.error(message)
      },
    }),
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        retry: 1,
      },
    },
  })

  const router = createRouter({
    routeTree,
    context: { queryClient },
    scrollRestoration: true,
    defaultPreloadStaleTime: 0,
  })

  setupRouterSsrQueryIntegration({ router, queryClient })

  return router
}
