import type { PlayStatus } from './interaction'
import type { TagSummary } from './tag'

export interface ReviewSummary {
  id: string
  content: string | null
  haveSpoilers: boolean
  createdAt: string
  updatedAt: string
  likeCount: number
  commentCount: number
  isLikedByViewer: boolean | null
}

export interface PlayLogDetail {
  id: string
  createdAt: string
  updatedAt: string
  videoGameId: string
  title: string
  coverUrl: string | null
  releaseDate: string | null
  userId: string
  username: string
  userPicture: string | null
  status: PlayStatus
  isReplay: boolean
  timePlayed: number | null
  startDate: string | null
  endDate: string | null
  ownership: string | null
  platformId: string
  platformName: string
  score: number | null
  tags: Array<TagSummary>
  review: ReviewSummary | null
  isOwner: boolean
  isLikedByViewer: boolean | null
}
