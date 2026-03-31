import { queryOptions } from '@tanstack/react-query'
import type { LikeResponse, Review, ReviewsResponse } from '@/types/review'
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

export interface SubmitPlayLogReviewPayload {
  content: string
  haveSpoilers: boolean
}

export const submitPlayLogReview = async (
  playId: string,
  payload: SubmitPlayLogReviewPayload,
): Promise<Review> => {
  const res = await apiFetch(`/api/me/plays/${playId}/review`, {
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

export interface ReportResponse {
  id: string
  content: string
  createdAt: string
}

export const reportReview = async (
  reviewId: string,
  payload: { content: string },
): Promise<ReportResponse> => {
  const res = await apiFetch(`/api/reviews/${reviewId}/report`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  })
  if (res.status === 409) {
    throw new Error('You have already reported this review')
  }
  if (!res.ok) {
    throw new Error('Failed to report review')
  }
  return res.json()
}

export const toggleReviewLike = async (
  reviewId: string,
): Promise<LikeResponse> => {
  const res = await apiFetch(`/api/reviews/${reviewId}/like`, {
    method: 'POST',
  })
  if (!res.ok) {
    throw new Error('Failed to toggle review like')
  }
  return res.json()
}
