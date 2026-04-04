import { queryOptions } from '@tanstack/react-query'
import type { Tag, TagRequestDto } from '@/types/tag'
import type { PlayLogListResponse } from '@/types/collection'
import { apiFetch } from '@/services/api'

// Query options

export function myTagsQueryOptions() {
  return queryOptions({
    queryKey: ['tags', 'me'],
    queryFn: async (): Promise<Array<Tag>> => {
      const res = await apiFetch('/api/me/tags')
      if (!res.ok) throw new Error('Failed to fetch tags')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function userTagsQueryOptions(username: string) {
  return queryOptions({
    queryKey: ['users', username, 'tags'],
    queryFn: async (): Promise<Array<Tag>> => {
      const res = await apiFetch(`/api/users/${username}/tags`)
      if (!res.ok) throw new Error('Failed to fetch user tags')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function userTagGamesQueryOptions(
  username: string,
  tagName: string,
  page: number = 0,
  size: number = 20,
) {
  return queryOptions({
    queryKey: ['users', username, 'tags', tagName, 'games', page, size],
    queryFn: async (): Promise<PlayLogListResponse> => {
      const res = await apiFetch(
        `/api/users/${username}/tags/${encodeURIComponent(tagName)}/games?page=${page}&size=${size}`,
      )
      if (!res.ok) throw new Error('Failed to fetch tag games')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

// Mutations

export async function createTag(data: TagRequestDto): Promise<Tag> {
  const res = await apiFetch('/api/me/tags', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Failed to create tag')
  return res.json()
}

export async function updateTag(
  tagId: string,
  data: TagRequestDto,
): Promise<Tag> {
  const res = await apiFetch(`/api/me/tags/${tagId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Failed to update tag')
  return res.json()
}

export async function deleteTag(tagId: string): Promise<void> {
  const res = await apiFetch(`/api/me/tags/${tagId}`, {
    method: 'DELETE',
  })
  if (!res.ok && res.status !== 204) throw new Error('Failed to delete tag')
}
