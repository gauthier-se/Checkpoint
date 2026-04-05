import type { PaginationMetadata } from './game'

export interface CommentUser {
  id: string
  pseudo: string
  picture: string | null
}

export interface Comment {
  id: string
  content: string
  user: CommentUser
  createdAt: string
  updatedAt: string
  parentCommentId: string | null
  repliesCount: number
  likesCount: number
  hasLiked: boolean
}

export interface CommentsResponse {
  content: Array<Comment>
  metadata: PaginationMetadata
}
