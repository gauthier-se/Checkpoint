import { queryOptions } from '@tanstack/react-query'
import type {
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

export const notificationsQueryOptions = (
  page: number = 0,
  size: number = 20,
) => {
  return queryOptions({
    queryKey: ['notifications', 'list', page, size],
    queryFn: async (): Promise<NotificationsResponse> => {
      const res = await apiFetch(
        `/api/me/notifications?page=${page}&size=${size}`,
      )
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

export const fetchWsToken = async (): Promise<string> => {
  const res = await apiFetch('/api/auth/ws-token')
  if (!res.ok) {
    throw new Error('Failed to fetch WebSocket token')
  }
  const data: { token: string } = await res.json()
  return data.token
}
