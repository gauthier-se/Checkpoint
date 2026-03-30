import { queryOptions } from '@tanstack/react-query'
import type { GameListDetail, GameListsResponse } from '@/types/list'
import { apiFetch } from '@/services/api'

export async function createList(data: {
  title: string
  description?: string
  isPrivate?: boolean
}): Promise<GameListDetail> {
  const res = await apiFetch('/api/me/lists', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Failed to create list')
  return res.json()
}

export async function updateList(
  listId: string,
  data: { title?: string; description?: string; isPrivate?: boolean },
): Promise<GameListDetail> {
  const res = await apiFetch(`/api/me/lists/${listId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Failed to update list')
  return res.json()
}

export async function deleteList(listId: string): Promise<void> {
  const res = await apiFetch(`/api/me/lists/${listId}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('Failed to delete list')
}

export async function addGameToList(
  listId: string,
  videoGameId: string,
): Promise<GameListDetail> {
  const res = await apiFetch(`/api/me/lists/${listId}/games`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ videoGameId }),
  })
  if (!res.ok) throw new Error('Failed to add game to list')
  return res.json()
}

export async function removeGameFromList(
  listId: string,
  videoGameId: string,
): Promise<void> {
  const res = await apiFetch(`/api/me/lists/${listId}/games/${videoGameId}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('Failed to remove game from list')
}

export async function reorderListGames(
  listId: string,
  orderedVideoGameIds: Array<string>,
): Promise<GameListDetail> {
  const res = await apiFetch(`/api/me/lists/${listId}/games/reorder`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ orderedVideoGameIds }),
  })
  if (!res.ok) throw new Error('Failed to reorder games')
  return res.json()
}

export function recentListsQueryOptions(page: number = 0, size: number = 20) {
  return queryOptions({
    queryKey: ['lists', 'recent', page, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(`/api/lists?page=${page}&size=${size}`)
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
      const res = await apiFetch(`/api/lists/popular?page=${page}&size=${size}`)
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
      const res = await apiFetch(`/api/me/lists?page=${page}&size=${size}`)
      if (!res.ok) throw new Error('Failed to fetch my lists')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}
