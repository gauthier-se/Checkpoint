import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import type { Notification, UnreadCountResponse } from '@/types/notification'
import { useAuth } from '@/hooks/use-auth'
import { fetchWsToken } from '@/queries/notifications'

const API_URL = import.meta.env.VITE_API_URL
const WS_URL = API_URL.replace(/^http/, 'ws') + '/ws/websocket'

export function useNotificationsWebSocket() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (typeof window === 'undefined' || !user) {
      return
    }

    let cancelled = false

    const connect = async () => {
      let token: string
      try {
        token = await fetchWsToken()
      } catch {
        return
      }

      if (cancelled) return

      const stompClient = new Client({
        brokerURL: WS_URL,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 5000,
        onConnect: () => {
          stompClient.subscribe('/user/queue/notifications', (message) => {
            const notification: Notification = JSON.parse(message.body)

            queryClient.setQueryData<UnreadCountResponse>(
              ['notifications', 'unread-count'],
              (old) => ({ count: (old?.count ?? 0) + 1 }),
            )

            queryClient.invalidateQueries({
              queryKey: ['notifications', 'list'],
            })

            toast(notification.message, {
              description: notification.senderPseudo ?? undefined,
            })
          })
        },
        onStompError: () => {
          // Auto-reconnect is handled by @stomp/stompjs
        },
        onWebSocketClose: () => {
          // On reconnect, resync unread count
          queryClient.invalidateQueries({
            queryKey: ['notifications', 'unread-count'],
          })
        },
      })

      clientRef.current = stompClient
      stompClient.activate()
    }

    connect()

    return () => {
      cancelled = true
      if (clientRef.current) {
        clientRef.current.deactivate()
        clientRef.current = null
      }
    }
  }, [user, queryClient])
}
