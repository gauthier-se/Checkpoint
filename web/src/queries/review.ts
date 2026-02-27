import { queryOptions } from '@tanstack/react-query'
import type { Review, ReviewsResponse } from '@/types/review'
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

export const userReviewQueryOptions = (gameId: string) => {
  return queryOptions({
    queryKey: ['games', gameId, 'reviews', 'me'],
    queryFn: async (): Promise<Review | null> => {
      const res = await apiFetch(`/api/games/${gameId}/reviews/me`)
      if (res.status === 404) {
        return null // User hasn't reviewed yet
      }
      if (!res.ok) {
        throw new Error('Failed to fetch user review')
      }
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export interface SubmitReviewPayload {
  score: number
  content: string
  haveSpoilers: boolean
}

export const submitReview = async (
  gameId: string,
  payload: SubmitReviewPayload,
): Promise<Review> => {
  const res = await apiFetch(`/api/games/${gameId}/reviews`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error('Failed to submit review')
  }
  return res.json()
}
