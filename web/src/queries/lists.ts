import { queryOptions } from '@tanstack/react-query'
import type {
  GameListDetail,
  GameListsResponse,
  GameListsSearchParams,
} from '@/types/list'
import type { LikeResponse } from '@/types/review'
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
  return res.json()
}

export async function deleteList(listId: string): Promise<void> {
  await apiFetch(`/api/me/lists/${listId}`, {
    method: 'DELETE',
  })
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
  return res.json()
}

export async function removeGameFromList(
  listId: string,
  videoGameId: string,
): Promise<void> {
  await apiFetch(`/api/me/lists/${listId}/games/${videoGameId}`, {
    method: 'DELETE',
  })
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
  return res.json()
}

export function recentListsQueryOptions(page: number = 0, size: number = 20) {
  return queryOptions({
    queryKey: ['lists', 'recent', page, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(`/api/lists?page=${page}&size=${size}`)
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

/**
 * Builds the /api/lists URL from a typed criteria object, omitting empty params.
 * The {@code page} field is 1-based in the UI and converted to 0-based for the API.
 */
export function buildListsUrl(criteria: GameListsSearchParams, size: number) {
  const params = new URLSearchParams()
  const uiPage = Number.isFinite(criteria.page) ? Math.floor(criteria.page) : 1
  params.set('page', String(Math.max(0, uiPage - 1)))
  params.set('size', String(size))

  const append = (key: string, value: string | undefined) => {
    if (value !== undefined && value !== '') {
      params.set(key, value)
    }
  }

  append('q', criteria.q)
  append('sort', criteria.sort)
  append('visibility', criteria.visibility)
  append('author', criteria.author)
  if (criteria.minGames !== undefined && criteria.minGames > 0) {
    params.set('minGames', String(criteria.minGames))
  }

  return `/api/lists?${params.toString()}`
}

export function searchListsQueryOptions(
  criteria: GameListsSearchParams,
  size: number = 20,
) {
  return queryOptions({
    queryKey: ['lists', 'search', criteria, size],
    queryFn: async (): Promise<GameListsResponse> => {
      const res = await apiFetch(buildListsUrl(criteria, size))
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
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export const toggleListLike = async (listId: string): Promise<LikeResponse> => {
  const res = await apiFetch(`/api/lists/${listId}/like`, {
    method: 'POST',
  })
  return res.json()
}
