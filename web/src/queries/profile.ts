import { queryOptions } from '@tanstack/react-query'
import type { UserProfile } from '@/types/profile'
import type { ReviewsResponse } from '@/types/review'
import type { PaginationMetadata } from '@/types/game'
import { apiFetch } from '@/services/api'

export interface WishlistItem {
  id: string
  videoGameId: string
  title: string
  coverUrl: string | null
  releaseDate: string | null
  addedAt: string
}

export interface WishlistResponse {
  content: Array<WishlistItem>
  metadata: PaginationMetadata
}

export interface FollowingUser {
  id: string
  pseudo: string
  picture: string | null
}

export interface FollowingResponse {
  content: Array<FollowingUser>
  metadata: PaginationMetadata
}

export const userProfileQueryOptions = (username: string) => {
  return queryOptions({
    queryKey: ['users', username, 'profile'],
    queryFn: async (): Promise<UserProfile> => {
      const res = await apiFetch(`/api/users/${username}`)
      if (!res.ok) {
        throw new Error('Failed to fetch user profile')
      }
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const userReviewsQueryOptions = (
  username: string,
  page: number = 0,
  size: number = 10,
) => {
  return queryOptions({
    queryKey: ['users', username, 'reviews', page, size],
    queryFn: async (): Promise<ReviewsResponse> => {
      const res = await apiFetch(
        `/api/users/${username}/reviews?page=${page}&size=${size}`,
      )
      if (!res.ok) {
        throw new Error('Failed to fetch user reviews')
      }
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const userWishlistQueryOptions = (
  username: string,
  page: number = 0,
  size: number = 20,
) => {
  return queryOptions({
    queryKey: ['users', username, 'wishlist', page, size],
    queryFn: async (): Promise<WishlistResponse> => {
      const res = await apiFetch(
        `/api/users/${username}/wishlist?page=${page}&size=${size}`,
      )
      if (!res.ok) {
        throw new Error('Failed to fetch user wishlist')
      }
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const userFollowingQueryOptions = (
  userId: string,
  page: number = 0,
  size: number = 20,
) => {
  return queryOptions({
    queryKey: ['users', userId, 'following', page, size],
    queryFn: async (): Promise<FollowingResponse> => {
      const res = await apiFetch(
        `/api/users/${userId}/following?page=${page}&size=${size}`,
      )
      if (!res.ok) {
        throw new Error('Failed to fetch following list')
      }
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const toggleFollowMutation = async (userId: string): Promise<void> => {
  const res = await apiFetch(`/api/users/${userId}/follow`, {
    method: 'POST',
  })
  if (!res.ok) {
    throw new Error('Failed to toggle follow')
  }
}
