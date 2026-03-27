import { queryOptions } from '@tanstack/react-query'
import type { Genre, Platform } from '@/types/game'
import { apiFetch } from '@/services/api'

export function genresQueryOptions() {
  return queryOptions({
    queryKey: ['genres'],
    queryFn: async (): Promise<Array<Genre>> => {
      const res = await apiFetch('/api/genres')
      if (!res.ok) throw new Error('Failed to fetch genres')
      return res.json()
    },
    staleTime: 5 * 60 * 1000,
  })
}

export function platformsQueryOptions() {
  return queryOptions({
    queryKey: ['platforms'],
    queryFn: async (): Promise<Array<Platform>> => {
      const res = await apiFetch('/api/platforms')
      if (!res.ok) throw new Error('Failed to fetch platforms')
      return res.json()
    },
    staleTime: 5 * 60 * 1000,
  })
}
