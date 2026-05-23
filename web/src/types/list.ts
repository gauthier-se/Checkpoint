import type { PaginationMetadata } from './game'

export interface GameListCard {
  id: string
  title: string
  description: string | null
  isPrivate: boolean
  videoGamesCount: number
  likesCount: number
  commentsCount: number
  authorPseudo: string
  authorPicture: string | null
  coverUrls: Array<string>
  createdAt: string
}

export interface GameListEntry {
  videoGameId: string
  title: string
  coverUrl: string
  releaseDate: string
  position: number
  addedAt: string
}

export interface GameListDetail {
  id: string
  title: string
  description: string | null
  isPrivate: boolean
  videoGamesCount: number
  likesCount: number
  commentsCount: number
  authorPseudo: string
  authorPicture: string | null
  entries: Array<GameListEntry>
  isOwner: boolean
  hasLiked: boolean
  createdAt: string
  updatedAt: string
}

export interface GameListsResponse {
  content: Array<GameListCard>
  metadata: PaginationMetadata
}

export type GameListSortOption = 'recent' | 'popular' | 'most-games'

export type GameListVisibility = 'public' | 'mine'

export type GameListsSearchParams = {
  page: number
  q?: string
  sort?: GameListSortOption
  visibility?: GameListVisibility
  author?: string
  minGames?: number
}
