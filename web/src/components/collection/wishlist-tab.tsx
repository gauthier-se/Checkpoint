import {
  queryOptions,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { Heart, Trash2 } from 'lucide-react'
import { useState } from 'react'
import type { Priority, WishlistResponse } from '@/types/collection'
import { CollectionGameCard } from '@/components/collection/collection-game-card'
import { CollectionPagination } from '@/components/collection/collection-pagination'
import { EmptyState } from '@/components/collection/empty-state'
import { PriorityBadge } from '@/components/collection/priority-badge'
import { PrioritySelect } from '@/components/collection/priority-select'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { updateWishlistPriority } from '@/queries/games'
import { apiFetch } from '@/services/api'

const PAGE_SIZE = 20

type WishlistSort = 'addedAt' | 'priority'

const SORT_PARAM: Record<WishlistSort, string> = {
  addedAt: 'createdAt,desc',
  priority: 'priority,desc',
}

export function wishlistQuery(page: number, sort: WishlistSort = 'addedAt') {
  return queryOptions({
    queryKey: ['wishlist', 'me', page, sort],
    queryFn: async (): Promise<WishlistResponse> => {
      const apiPage = page - 1
      const res = await apiFetch(
        `/api/me/wishlist?page=${apiPage}&size=${PAGE_SIZE}&sort=${SORT_PARAM[sort]}`,
      )
      if (!res.ok) throw new Error('Failed to load wishlist')
      return res.json()
    },
  })
}

interface WishlistTabProps {
  page: number
}

export function WishlistTab({ page }: WishlistTabProps) {
  const [sort, setSort] = useState<WishlistSort>('addedAt')
  const { data, isLoading, isError } = useQuery(wishlistQuery(page, sort))
  const queryClient = useQueryClient()

  const removeMutation = useMutation({
    mutationFn: async (videoGameId: string) => {
      const res = await apiFetch(`/api/me/wishlist/${videoGameId}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Failed to remove from wishlist')
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['wishlist', 'me'] })
    },
  })

  const priorityMutation = useMutation({
    mutationFn: ({
      videoGameId,
      priority,
    }: {
      videoGameId: string
      priority: Priority | null
    }) => updateWishlistPriority(videoGameId, priority),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['wishlist', 'me'] })
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
        icon={<Heart className="size-12" />}
        title="Unable to load wishlist"
        description="The wishlist feature is not available yet. Check back soon!"
      />
    )
  }

  if (data.content.length === 0) {
    return (
      <EmptyState
        icon={<Heart className="size-12" />}
        title="Your wishlist is empty"
        description="Browse games to find titles you'd love to play someday and add them to your wishlist!"
        actionLabel="Browse Games"
        actionTo="/games"
      />
    )
  }

  return (
    <div>
      <div className="mb-4 flex items-center justify-end gap-2">
        <span className="text-xs text-muted-foreground">Sort by</span>
        <Select
          value={sort}
          onValueChange={(value) => setSort(value as WishlistSort)}
        >
          <SelectTrigger size="sm" className="h-8 w-[160px] text-xs">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="addedAt">Date added</SelectItem>
            <SelectItem value="priority">Priority</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        {data.content.map((game) => (
          <CollectionGameCard
            key={game.id}
            videoGameId={game.videoGameId}
            title={game.title}
            coverUrl={game.coverUrl}
          >
            <div className="flex flex-wrap items-center gap-1.5">
              {game.releaseDate && (
                <p className="text-xs text-muted-foreground">
                  {new Date(game.releaseDate).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                  })}
                </p>
              )}
              <PriorityBadge priority={game.priority} />
            </div>
            <div className="mt-auto flex flex-col gap-1 pt-2">
              <PrioritySelect
                value={game.priority}
                disabled={priorityMutation.isPending}
                onChange={(priority) =>
                  priorityMutation.mutate({
                    videoGameId: game.videoGameId,
                    priority,
                  })
                }
              />
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
        tab="wishlist"
        page={page}
        totalPages={data.metadata.totalPages}
        hasNext={data.metadata.hasNext}
        hasPrevious={data.metadata.hasPrevious}
      />
    </div>
  )
}
