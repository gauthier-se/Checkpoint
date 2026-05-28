export interface BadgeDto {
  id: string
  code: string
  name: string
  picture: string | null
  description: string | null
  hidden: boolean
  earned: boolean
}

export interface FavoriteGame {
  gameId: string
  title: string
  coverUrl: string | null
  displayOrder: number
}

export interface RecentPlay {
  id: string
  videoGameId: string
  title: string
  coverUrl: string | null
  score: number | null
  hasReview: boolean
  isReplay: boolean
  isLiked: boolean
  createdAt: string
}

export interface UserProfile {
  id: string
  username: string
  bio: string | null
  picture: string | null
  level: number
  xpPoint: number
  xpThreshold: number
  isPrivate: boolean
  badges: Array<BadgeDto>
  favorites: Array<FavoriteGame>
  recentPlays: Array<RecentPlay>
  followerCount: number
  followingCount: number
  reviewCount: number
  wishlistCount: number
  isFollowing: boolean | null
  isOwner: boolean
  createdAt: string
}
