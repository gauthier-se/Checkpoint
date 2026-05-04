export interface NotificationPreferences {
  followEnabled: boolean
  likeReviewEnabled: boolean
  likeListEnabled: boolean
  likeGameEnabled: boolean
  commentReplyEnabled: boolean
}

export type UpdateNotificationPreferences = Partial<NotificationPreferences>
