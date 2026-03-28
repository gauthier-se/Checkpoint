import type { PaginationMetadata } from './game'

export interface MemberCard {
  id: string
  pseudo: string
  picture: string | null
  level: number
  followerCount: number
  reviewCount: number
  isFollowing: boolean | null
}

export interface MembersResponse {
  content: Array<MemberCard>
  metadata: PaginationMetadata
}

export type MembersSearchParams = {
  page: number
  search?: string
}
