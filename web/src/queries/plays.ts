import { queryOptions } from '@tanstack/react-query'
import type { GamePlayLogRequestDto } from '@/types/interaction'
import type { PlayLogDetail } from '@/types/play-log'
import { apiFetch } from '@/services/api'

export const playLogDetailQueryOptions = (playId: string) => {
  return queryOptions({
    queryKey: ['plays', playId],
    queryFn: async (): Promise<PlayLogDetail> => {
      const res = await apiFetch(`/api/plays/${playId}`)
      return res.json()
    },
    staleTime: 0,
  })
}

export async function updatePlayLog(
  playId: string,
  request: GamePlayLogRequestDto,
): Promise<PlayLogDetail> {
  const res = await apiFetch(`/api/me/plays/${playId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return res.json()
}

export async function deletePlayLog(playId: string): Promise<void> {
  await apiFetch(`/api/me/plays/${playId}`, { method: 'DELETE' })
}
