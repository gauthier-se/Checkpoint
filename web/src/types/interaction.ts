import type { GameStatus } from './library'

export interface GameInteractionStatusDto {
  inWishlist: boolean
  inBacklog: boolean
  inLibrary: boolean
  libraryStatus: GameStatus | null
  playCount: number
  userRating: number | null
  hasReview: boolean
}

export type PlayStatus = 'PLAYING' | 'PAUSED' | 'DROPPED' | 'COMPLETED'

export interface GamePlayLogRequestDto {
  videoGameId: string
  platformId: string
  status?: PlayStatus
  startDate?: string
  endDate?: string
  timePlayed?: number
  ownership?: string
  isReplay?: boolean
}

export interface GamePlayLogResponseDto {
  id: string
  videoGameId: string
  platformId: string
  status: PlayStatus
  startDate: string | null
  endDate: string | null
  timePlayed: number
  ownership: string | null
  isReplay: boolean
  createdAt: string
  updatedAt: string
}
