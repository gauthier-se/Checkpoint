import { queryOptions } from '@tanstack/react-query'
import type { GameListDetail, GameListsResponse } from '@/types/list'
import { apiFetch } from '@/services/api'

export function recentListsQueryOptions(page: number = 0, size: number = 20) {
  return queryOptions({
    queryKey: ['lists', 'recent', page, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(
        `/api/lists?page=${page}&size=${size}`,
      )
      if (!res.ok) throw new Error('Failed to fetch recent lists')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function popularListsQueryOptions(page: number = 0, size: number = 5) {
  return queryOptions({
    queryKey: ['lists', 'popular', page, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(
        `/api/lists/popular?page=${page}&size=${size}`,
      )
      if (!res.ok) throw new Error('Failed to fetch popular lists')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function listDetailQueryOptions(listId: string) {
  return queryOptions({
    queryKey: ['lists', listId, 'detail'],
    queryFn: async (): Promise<GameListDetail> => {
      const res = await apiFetch(`/api/lists/${listId}`)
      if (!res.ok) throw new Error('Failed to fetch list details')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function userListsQueryOptions(
  username: string,
  page: number = 0,
  size: number = 20,
) {
  return queryOptions({
    queryKey: ['users', username, 'lists', page, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(
        `/api/users/${username}/lists?page=${page}&size=${size}`,
      )
      if (!res.ok) throw new Error('Failed to fetch user lists')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function myListsQueryOptions(page: number = 0, size: number = 20) {
  return queryOptions({
    queryKey: ['lists', 'mine', page, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(
        `/api/me/lists?page=${page}&size=${size}`,
      )
      if (!res.ok) throw new Error('Failed to fetch my lists')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}
