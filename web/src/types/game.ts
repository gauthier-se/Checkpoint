export interface Game {
  id: string
  title: string
  coverUrl: string
  releaseDate: string
  averageRating: number | null
  ratingCount: number
}

export interface Genre {
  id: string
  name: string
}

export interface Platform {
  id: string
  name: string
}

export interface Company {
  id: string
  name: string
}

export interface GameDetail {
  id: string
  title: string
  description: string | null
  coverUrl: string
  releaseDate: string
  averageRating: number | null
  ratingCount: number
  genres: Genre[]
  platforms: Platform[]
  companies: Company[]
}

export interface PaginationMetadata {
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
}

export interface GamesResponse {
  content: Game[]
  metadata: PaginationMetadata
}
