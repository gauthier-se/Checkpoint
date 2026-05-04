import { queryOptions } from '@tanstack/react-query'
import type {
  NotificationPreferences,
  UpdateNotificationPreferences,
} from '@/types/notification-preferences'
import { apiFetch } from '@/services/api'

export const notificationPreferencesQueryOptions = () => {
  return queryOptions({
    queryKey: ['notification-preferences'],
    queryFn: async (): Promise<NotificationPreferences> => {
      const res = await apiFetch('/api/me/notification-preferences')
      if (!res.ok) {
        throw new Error('Failed to fetch notification preferences')
      }
      return res.json()
    },
    staleTime: 0,
  })
}

export async function updateNotificationPreferences(
  data: UpdateNotificationPreferences,
): Promise<NotificationPreferences> {
  const res = await apiFetch('/api/me/notification-preferences', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const error = await res.json().catch(() => null)
    throw new Error(
      error?.message || 'Failed to update notification preferences',
    )
  }
  return res.json()
}
