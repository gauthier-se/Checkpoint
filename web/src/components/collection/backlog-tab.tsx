import {
  queryOptions,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { Archive, ArrowRightLeft, Trash2 } from 'lucide-react'
import type { BacklogListResponse } from '@/types/collection'
import { CollectionGameCard } from '@/components/collection/collection-game-card'
import { CollectionPagination } from '@/components/collection/collection-pagination'
import { EmptyState } from '@/components/collection/empty-state'
import { Button } from '@/components/ui/button'
import { apiFetch } from '@/services/api'

const PAGE_SIZE = 20

export function backlogQuery(page: number) {
  return queryOptions({
    queryKey: ['backlog', 'me', page],
    queryFn: async (): Promise<BacklogListResponse> => {
      const apiPage = page - 1
      const res = await apiFetch(
        `/api/me/backlog?page=${apiPage}&size=${PAGE_SIZE}`,
      )
      if (!res.ok) throw new Error('Failed to load backlog')
      return res.json()
    },
  })
}

interface BacklogTabProps {
  page: number
}

export function BacklogTab({ page }: BacklogTabProps) {
  const { data, isLoading, isError } = useQuery(backlogQuery(page))
  const queryClient = useQueryClient()

  const removeMutation = useMutation({
    mutationFn: async (videoGameId: string) => {
      const res = await apiFetch(`/api/me/backlog/${videoGameId}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Failed to remove from backlog')
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['backlog', 'me'] })
    },
  })

  const moveToLibraryMutation = useMutation({
    mutationFn: async (videoGameId: string) => {
      // Add to library with PLAYING status
      const res = await apiFetch('/api/me/library', {
        method: 'POST',
        body: JSON.stringify({ videoGameId, status: 'PLAYING' }),
        headers: { 'Content-Type': 'application/json' },
      })
      if (!res.ok) throw new Error('Failed to add to library')
      // Remove from backlog
      await apiFetch(`/api/me/backlog/${videoGameId}`, { method: 'DELETE' })
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['backlog', 'me'] })
      void queryClient.invalidateQueries({ queryKey: ['library', 'me'] })
    },
  })

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        {Array.from({ length: 10 }).map((_, i) => (
          <div key={i} className="flex flex-col gap-2 rounded-lg border p-3">
            <div className="aspect-[3/4] animate-pulse rounded-md bg-muted" />
            <div className="h-4 w-3/4 animate-pulse rounded bg-muted" />
          </div>
        ))}
      </div>
    )
  }

  if (isError || !data) {
    return (
      <EmptyState
        icon={<Archive className="size-12" />}
        title="Unable to load backlog"
        description="The backlog feature is not available yet. Check back soon!"
      />
    )
  }

  if (data.content.length === 0) {
    return (
      <EmptyState
        icon={<Archive className="size-12" />}
        title="Your backlog is empty"
        description="Found a game you want to play later? Add it to your backlog to keep track!"
        actionLabel="Browse Games"
        actionTo="/games"
      />
    )
  }

  return (
    <div>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        {data.content.map((game) => (
          <CollectionGameCard
            key={game.id}
            videoGameId={game.videoGameId}
            title={game.title}
            coverUrl={game.coverUrl}
          >
            {game.releaseDate && (
              <p className="text-xs text-muted-foreground">
                {new Date(game.releaseDate).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'short',
                  day: 'numeric',
                })}
              </p>
            )}
            <div className="mt-auto flex flex-col gap-1 pt-2">
              <Button
                variant="outline"
                size="sm"
                className="h-7 w-full gap-1.5 text-xs"
                disabled={moveToLibraryMutation.isPending}
                onClick={() => moveToLibraryMutation.mutate(game.videoGameId)}
              >
                <ArrowRightLeft className="size-3" />
                Move to Library
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="h-7 w-full gap-1.5 text-xs text-destructive hover:text-destructive"
                disabled={removeMutation.isPending}
                onClick={() => removeMutation.mutate(game.videoGameId)}
              >
                <Trash2 className="size-3" />
                Remove
              </Button>
            </div>
          </CollectionGameCard>
        ))}
      </div>
      <CollectionPagination
        tab="backlog"
        page={page}
        totalPages={data.metadata.totalPages}
        hasNext={data.metadata.hasNext}
        hasPrevious={data.metadata.hasPrevious}
      />
    </div>
  )
}
