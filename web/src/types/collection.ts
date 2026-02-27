import type { PaginationMetadata } from './game'

// Wishlist
export interface WishResponse {
  id: string
  videoGameId: string
  title: string
  coverUrl: string | null
  releaseDate: string | null
  addedAt: string
}

export interface WishlistResponse {
  content: Array<WishResponse>
  metadata: PaginationMetadata
}

// Backlog
export interface BacklogResponse {
  id: string
  videoGameId: string
  title: string
  coverUrl: string | null
  releaseDate: string | null
  addedAt: string
}

export interface BacklogListResponse {
  content: Array<BacklogResponse>
  metadata: PaginationMetadata
}

// Play Log
export type PlayStatus =
  | 'ARE_PLAYING'
  | 'PLAYED'
  | 'COMPLETED'
  | 'RETIRED'
  | 'SHELVED'
  | 'ABANDONED'

export interface PlayLogResponse {
  id: string
  videoGameId: string
  title: string
  coverUrl: string | null
  platformId: string | null
  platformName: string | null
  status: PlayStatus
  isReplay: boolean
  timePlayed: number | null
  startDate: string | null
  endDate: string | null
  ownership: string | null
  createdAt: string
  updatedAt: string
}

export interface PlayLogListResponse {
  content: Array<PlayLogResponse>
  metadata: PaginationMetadata
}

// Shared
export type CollectionTab = 'library' | 'wishlist' | 'backlog' | 'playlog'
