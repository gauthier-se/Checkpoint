import {
  queryOptions,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { Heart, Trash2 } from 'lucide-react'
import type { WishlistResponse } from '@/types/collection'
import { CollectionGameCard } from '@/components/collection/collection-game-card'
import { CollectionPagination } from '@/components/collection/collection-pagination'
import { EmptyState } from '@/components/collection/empty-state'
import { Button } from '@/components/ui/button'
import { apiFetch } from '@/services/api'

const PAGE_SIZE = 20

export function wishlistQuery(page: number) {
  return queryOptions({
    queryKey: ['wishlist', 'me', page],
    queryFn: async (): Promise<WishlistResponse> => {
      const apiPage = page - 1
      const res = await apiFetch(
        `/api/me/wishlist?page=${apiPage}&size=${PAGE_SIZE}`,
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
  const { data, isLoading, isError } = useQuery(wishlistQuery(page))
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
            <div className="mt-auto pt-2">
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
