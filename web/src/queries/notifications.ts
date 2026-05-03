import { queryOptions } from '@tanstack/react-query'
import type {
  NotificationType,
  NotificationsResponse,
  UnreadCountResponse,
} from '@/types/notification'
import { apiFetch } from '@/services/api'

export const unreadCountQueryOptions = () => {
  return queryOptions({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async (): Promise<UnreadCountResponse> => {
      const res = await apiFetch('/api/me/notifications/unread-count')
      if (!res.ok) {
        throw new Error('Failed to fetch unread count')
      }
      return res.json()
    },
    staleTime: 30 * 1000,
    refetchInterval: 60 * 1000,
  })
}

export interface NotificationsQueryParams {
  page?: number
  size?: number
  type?: NotificationType
  isRead?: boolean
}

export const notificationsQueryOptions = (
  params: NotificationsQueryParams = {},
) => {
  const { page = 0, size = 20, type, isRead } = params

  const qs = new URLSearchParams()
  qs.set('page', String(page))
  qs.set('size', String(size))
  if (type) qs.set('type', type)
  if (isRead !== undefined) qs.set('isRead', String(isRead))

  return queryOptions({
    queryKey: ['notifications', 'list', { page, size, type, isRead }],
    queryFn: async (): Promise<NotificationsResponse> => {
      const res = await apiFetch(`/api/me/notifications?${qs.toString()}`)
      if (!res.ok) {
        throw new Error('Failed to fetch notifications')
      }
      return res.json()
    },
    staleTime: 30 * 1000,
  })
}

export const markNotificationAsRead = async (
  notificationId: string,
): Promise<void> => {
  const res = await apiFetch(`/api/me/notifications/${notificationId}/read`, {
    method: 'PUT',
  })
  if (!res.ok) {
    throw new Error('Failed to mark notification as read')
  }
}

export const markAllNotificationsAsRead = async (): Promise<void> => {
  const res = await apiFetch('/api/me/notifications/read-all', {
    method: 'PUT',
  })
  if (!res.ok) {
    throw new Error('Failed to mark all notifications as read')
  }
}

export const markBulkNotificationsAsRead = async (
  ids: Array<string>,
): Promise<void> => {
  const res = await apiFetch('/api/me/notifications/mark-read', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ids }),
  })
  if (!res.ok) {
    throw new Error('Failed to mark notifications as read')
  }
}

export const fetchWsToken = async (): Promise<string> => {
  const res = await apiFetch('/api/auth/ws-token')
  if (!res.ok) {
    throw new Error('Failed to fetch WebSocket token')
  }
  const data: { token: string } = await res.json()
  return data.token
}
