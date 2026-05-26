import type { PaginationMetadata } from './game'

export type FeedItemType = 'PLAY' | 'RATING' | 'REVIEW' | 'LIST' | 'LIKE_GAME'

/** All concrete feed item types, used to validate the `type` query/search param. */
export const FEED_ITEM_TYPES: ReadonlyArray<FeedItemType> = [
  'PLAY',
  'RATING',
  'REVIEW',
  'LIST',
  'LIKE_GAME',
]

/** Selected feed filter tab: a concrete type or `'all'` (no filter). */
export type FeedTab = 'all' | FeedItemType

export interface FeedUser {
  id: string
  pseudo: string
  picture: string | null
}

export interface FeedGame {
  id: string
  title: string
  coverUrl: string
  releaseDate: string | null
}

export interface FeedItem {
  id: string
  type: FeedItemType
  createdAt: string
  user: FeedUser
  game: FeedGame | null
  playStatus?: string
  score?: number
  reviewContent?: string
  haveSpoilers?: boolean
  listTitle?: string
  listGameCount?: number
  /** Associated play-log id, used to link a REVIEW item to its log detail page. */
  logId?: string
}

export interface FeedResponse {
  content: Array<FeedItem>
  metadata: PaginationMetadata
}
