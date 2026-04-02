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
}

export interface CommentsResponse {
  content: Array<Comment>
  metadata: PaginationMetadata
}
