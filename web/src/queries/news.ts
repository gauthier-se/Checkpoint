import { queryOptions } from '@tanstack/react-query'
import type { NewsArticle, NewsResponse } from '@/types/news'
import { apiFetch } from '@/services/api'

export function newsListQueryOptions(page: number = 0, size: number = 20) {
  return queryOptions({
    queryKey: ['news', 'list', page, size],
    queryFn: async (): Promise<NewsResponse> => {
      const res = await apiFetch(`/api/news?page=${page}&size=${size}`)
      if (!res.ok) throw new Error('Failed to fetch news')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}

export function newsDetailQueryOptions(newsId: string) {
  return queryOptions({
    queryKey: ['news', newsId],
    queryFn: async (): Promise<NewsArticle> => {
      const res = await apiFetch(`/api/news/${newsId}`)
      if (!res.ok) throw new Error('Failed to fetch news article')
      return res.json()
    },
    staleTime: 60 * 1000,
  })
}
