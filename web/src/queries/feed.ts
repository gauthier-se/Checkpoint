import { queryOptions } from '@tanstack/react-query'
import type { Game } from '@/types/game'
import type { FeedResponse } from '@/types/feed'
import { apiFetch } from '@/services/api'

export function feedQueryOptions(page: number = 0, size: number = 5) {
  return queryOptions({
    queryKey: ['feed', page, size],
    queryFn: async (): Promise<FeedResponse> => {
      const res = await apiFetch(`/api/me/feed?page=${page}&size=${size}`)
      if (!res.ok) throw new Error('Failed to fetch feed')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function friendsTrendingGamesQueryOptions(size: number = 7) {
  return queryOptions({
    queryKey: ['games', 'friends-trending', size],
    queryFn: async (): Promise<Array<Game>> => {
      const res = await apiFetch(
        `/api/me/friends/trending-games?size=${size}`,
      )
      if (!res.ok) throw new Error('Failed to fetch friends trending games')
      return res.json()
    },
    staleTime: 5 * 60 * 1000,
  })
}
