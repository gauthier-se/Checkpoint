import {
  queryOptions,
  useMutation,
  useQueryClient,
  useSuspenseQuery,
} from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import { useState } from 'react'
import type { GameStatus, LibraryResponse } from '@/types/library'
import { Button } from '@/components/ui/button'
import { ButtonGroup } from '@/components/ui/button-group'
import { apiFetch } from '@/services/api'

export const libraryQueryOptions = queryOptions({
  queryKey: ['library', 'me'],
  queryFn: async (): Promise<LibraryResponse> => {
    const res = await apiFetch(`/api/me/library?page=0&size=100`)
    if (!res.ok) {
      throw new Error('Failed to load library')
    }
    return res.json()
  },
})

export const Route = createFileRoute('/_app/_protected/$username/games/')({
  component: UserGamesPage,
  loader: async ({ context }) => {
    return context.queryClient.ensureQueryData(libraryQueryOptions)
  },
})

function UserGamesPage() {
  const { data } = useSuspenseQuery(libraryQueryOptions)
  const queryClient = useQueryClient()
  const [filter, setFilter] = useState<GameStatus | 'ALL'>('ALL')

  const filteredGames = data.content.filter(
    (game) => filter === 'ALL' || game.status === filter,
  )

  const updateStatusMutation = useMutation({
    mutationFn: async ({
      gameId,
      status,
    }: {
      gameId: string
      status: GameStatus
    }) => {
      const res = await apiFetch(`/api/me/library/${gameId}`, {
        method: 'PUT',
        body: JSON.stringify({ videoGameId: gameId, status }),
        headers: { 'Content-Type': 'application/json' },
      })
      if (!res.ok) throw new Error('Failed to update status')
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['library', 'me'] })
    },
  })

  const removeGameMutation = useMutation({
    mutationFn: async (gameId: string) => {
      const res = await apiFetch(`/api/me/library/${gameId}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Failed to remove game')
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['library', 'me'] })
    },
  })

  return (
    <main className="max-w-7xl mx-auto py-10 px-4">
      <h1 className="text-3xl font-bold mb-8">My Games</h1>

      <div className="mb-8 flex space-x-2">
        <ButtonGroup>
          <Button
            variant={filter === 'ALL' ? 'default' : 'outline'}
            onClick={() => setFilter('ALL')}
          >
            All
          </Button>
          <Button
            variant={filter === 'BACKLOG' ? 'default' : 'outline'}
            onClick={() => setFilter('BACKLOG')}
          >
            Backlog
          </Button>
          <Button
            variant={filter === 'PLAYING' ? 'default' : 'outline'}
            onClick={() => setFilter('PLAYING')}
          >
            Playing
          </Button>
          <Button
            variant={filter === 'COMPLETED' ? 'default' : 'outline'}
            onClick={() => setFilter('COMPLETED')}
          >
            Completed
          </Button>
        </ButtonGroup>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {filteredGames.length === 0 ? (
          <p className="text-muted-foreground col-span-full py-8 text-center">
            No games found.
          </p>
        ) : (
          filteredGames.map((game) => (
            <div
              key={game.id}
              className="group relative flex flex-col gap-2 rounded-lg border bg-card p-4 text-card-foreground shadow-sm"
            >
              <div className="aspect-[3/4] overflow-hidden rounded-md bg-muted">
                {game.coverUrl ? (
                  <img
                    src={game.coverUrl}
                    alt={game.title}
                    className="h-full w-full object-cover transition-all group-hover:scale-105"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center bg-secondary">
                    <span className="text-muted-foreground">No Cover</span>
                  </div>
                )}
              </div>
              <h3 className="font-semibold line-clamp-1">{game.title}</h3>
              <p className="text-xs text-muted-foreground">{game.status}</p>

              <div className="mt-auto pt-4 flex gap-2">
                <button
                  className="text-xs text-blue-500 hover:underline disabled:opacity-50"
                  disabled={updateStatusMutation.isPending}
                  onClick={() =>
                    updateStatusMutation.mutate({
                      gameId: game.videoGameId,
                      status:
                        game.status === 'COMPLETED' ? 'BACKLOG' : 'COMPLETED',
                    })
                  }
                >
                  Toggle Status
                </button>
                <button
                  className="text-xs text-red-500 hover:underline disabled:opacity-50"
                  disabled={removeGameMutation.isPending}
                  onClick={() => removeGameMutation.mutate(game.videoGameId)}
                >
                  Remove
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </main>
  )
}
