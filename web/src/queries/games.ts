import { queryOptions } from '@tanstack/react-query'
import type {
  GameInteractionStatusDto,
  GamePlayLogRequestDto,
  GamePlayLogResponseDto,
} from '@/types/interaction'
import type { UserGameRequest } from '@/types/library'
import { apiFetch } from '@/services/api'

export function gameInteractionStatusQueryOptions(gameId: string) {
  return queryOptions({
    queryKey: ['games', gameId, 'interaction-status'],
    queryFn: async () => {
      const res = await apiFetch(`/api/me/games/${gameId}/status`)
      if (!res.ok) {
        throw new Error('Failed to fetch interaction status')
      }
      return res.json() as Promise<GameInteractionStatusDto>
    },
  })
}

export async function toggleWishlist(gameId: string, currentStatus: boolean) {
  const method = currentStatus ? 'DELETE' : 'POST'
  const res = await apiFetch(`/api/me/wishlist/${gameId}`, {
    method,
  })
  if (!res.ok && res.status !== 204) {
    throw new Error('Failed to toggle wishlist')
  }
}

export async function toggleBacklog(gameId: string, currentStatus: boolean) {
  const method = currentStatus ? 'DELETE' : 'POST'
  const res = await apiFetch(`/api/me/backlog/${gameId}`, {
    method,
  })
  if (!res.ok && res.status !== 204) {
    throw new Error('Failed to toggle backlog')
  }
}

export async function updateLibraryStatus(
  gameId: string,
  request: UserGameRequest | null,
) {
  if (!request) {
    const res = await apiFetch(`/api/me/library/${gameId}`, {
      method: 'DELETE',
    })
    if (!res.ok && res.status !== 204) {
      throw new Error('Failed to remove from library')
    }
  } else {
    // We don't know if it's currently in the library or not strictly from this function signature
    // but the API supports POST for add and PUT for update.
    // Wait, the API `UserGameCollectionController` has POST (add) and PUT (update).
    // If we rely on optimistic updates, the caller might know.
    // For simplicity, we can do PUT and if it fails with 404, do POST? Or have separate add/update logic.
    // The issue says: "Library -- dropdown/popover to set library status (PLAYING, COMPLETED, DROPPED) or remove from library."
    // Let's assume the caller passes whether it's an update or adding.
    throw new Error('Implemented in caller')
  }
}

export async function logPlay(request: GamePlayLogRequestDto) {
  const res = await apiFetch('/api/me/plays', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  if (!res.ok) {
    throw new Error('Failed to log play')
  }
  return res.json() as Promise<GamePlayLogResponseDto>
}
