import type { PaginationMetadata } from './game'

export interface ReviewUser {
  id: string
  pseudo: string
  picture: string | null
}

export interface Review {
  id: string
  score: number | null
  content: string
  haveSpoilers: boolean
  createdAt: string
  updatedAt: string
  user: ReviewUser
}

export interface ReviewsResponse {
  content: Array<Review>
  metadata: PaginationMetadata
}
