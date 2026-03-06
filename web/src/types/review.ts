import type { PaginationMetadata } from './game'
import type { PlayStatus } from './interaction'

export interface ReviewUser {
  id: string
  pseudo: string
  picture: string | null
}

export interface Review {
  id: string
  content: string
  haveSpoilers: boolean
  createdAt: string
  updatedAt: string
  user: ReviewUser
  playLogId: string | null
  platformName: string | null
  playStatus: PlayStatus | null
  isReplay: boolean | null
}

export interface ReviewsResponse {
  content: Array<Review>
  metadata: PaginationMetadata
}
