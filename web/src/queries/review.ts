import { queryOptions } from '@tanstack/react-query'
import type { ReviewsResponse } from '@/types/review'
import { apiFetch } from '@/services/api'

export const gameReviewsQueryOptions = (
  gameId: string,
  page: number = 0,
  size: number = 10,
) => {
  return queryOptions({
    queryKey: ['games', gameId, 'reviews', page, size],
    queryFn: async (): Promise<ReviewsResponse> => {
      const res = await apiFetch(
        `/api/games/${gameId}/reviews?page=${page}&size=${size}`,
      )
      if (!res.ok) {
        throw new Error('Failed to fetch reviews')
      }
      return res.json()
    },
    staleTime: 60 * 1000, // 1 minute
  })
}
