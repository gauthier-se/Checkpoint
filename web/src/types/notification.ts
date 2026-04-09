import type { PaginationMetadata } from './game'

export type NotificationType =
  | 'FOLLOW'
  | 'LIKE_REVIEW'
  | 'LIKE_LIST'
  | 'LIKE_GAME'
  | 'COMMENT_REPLY'

export interface Notification {
  id: string
  senderPseudo: string | null
  senderPicture: string | null
  type: NotificationType
  referenceId: string | null
  message: string
  isRead: boolean
  createdAt: string
}

export interface NotificationsResponse {
  content: Array<Notification>
  metadata: PaginationMetadata
}

export interface UnreadCountResponse {
  count: number
}
