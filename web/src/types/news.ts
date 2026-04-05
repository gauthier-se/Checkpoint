import type { PaginationMetadata } from './game'

export interface NewsAuthor {
  id: string
  pseudo: string
  picture: string | null
}

export interface NewsArticle {
  id: string
  title: string
  description: string
  picture: string | null
  publishedAt: string
  createdAt: string
  updatedAt: string
  author: NewsAuthor
}

export interface NewsResponse {
  content: Array<NewsArticle>
  metadata: PaginationMetadata
}
