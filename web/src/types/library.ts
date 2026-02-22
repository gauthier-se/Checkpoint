import type { PaginationMetadata } from './game'

export type GameStatus = 'BACKLOG' | 'PLAYING' | 'COMPLETED' | 'DROPPED'

export interface UserGameResponse {
  id: string
  videoGameId: string
  title: string
  coverUrl: string | null
  releaseDate: string | null
  status: GameStatus
  addedAt: string
  updatedAt: string
}

export interface UserGameRequest {
  videoGameId: string
  status: GameStatus
}

export interface LibraryResponse {
  content: Array<UserGameResponse>
  metadata: PaginationMetadata
}
