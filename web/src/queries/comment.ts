import { queryOptions } from '@tanstack/react-query'

import type { Comment, CommentsResponse } from '@/types/comment'
import type { LikeResponse } from '@/types/review'
import { apiFetch } from '@/services/api'

export const reviewCommentsQueryOptions = (
  reviewId: string,
  page: number = 0,
  size: number = 20,
) => {
  return queryOptions({
    queryKey: ['reviews', reviewId, 'comments', page, size],
    queryFn: async (): Promise<CommentsResponse> => {
      const res = await apiFetch(
        `/api/reviews/${reviewId}/comments?page=${page}&size=${size}`,
      )
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const listCommentsQueryOptions = (
  listId: string,
  page: number = 0,
  size: number = 20,
) => {
  return queryOptions({
    queryKey: ['lists', listId, 'comments', page, size],
    queryFn: async (): Promise<CommentsResponse> => {
      const res = await apiFetch(
        `/api/lists/${listId}/comments?page=${page}&size=${size}`,
      )
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const commentRepliesQueryOptions = (
  commentId: string,
  page: number = 0,
  size: number = 20,
) => {
  return queryOptions({
    queryKey: ['comments', commentId, 'replies', page, size],
    queryFn: async (): Promise<CommentsResponse> => {
      const res = await apiFetch(
        `/api/comments/${commentId}/replies?page=${page}&size=${size}`,
      )
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const postReviewComment = async (
  reviewId: string,
  content: string,
): Promise<Comment> => {
  const res = await apiFetch(`/api/reviews/${reviewId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }),
  })
  return res.json()
}

export const postListComment = async (
  listId: string,
  content: string,
): Promise<Comment> => {
  const res = await apiFetch(`/api/lists/${listId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }),
  })
  return res.json()
}

export const postReply = async (
  commentId: string,
  content: string,
): Promise<Comment> => {
  const res = await apiFetch(`/api/comments/${commentId}/replies`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }),
  })
  return res.json()
}

export const updateComment = async (
  commentId: string,
  content: string,
): Promise<Comment> => {
  const res = await apiFetch(`/api/comments/${commentId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }),
  })
  return res.json()
}

export const deleteComment = async (commentId: string): Promise<void> => {
  await apiFetch(`/api/comments/${commentId}`, {
    method: 'DELETE',
  })
}

export const toggleCommentLike = async (
  commentId: string,
): Promise<LikeResponse> => {
  const res = await apiFetch(`/api/comments/${commentId}/like`, {
    method: 'POST',
  })
  return res.json()
}
