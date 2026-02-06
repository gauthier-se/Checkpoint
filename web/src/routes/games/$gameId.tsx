import { useEffect } from 'react'

import { Separator } from '@/components/ui/separator'
import type { GameDetail } from '@/types/game'
import { createFileRoute, useRouter } from '@tanstack/react-router'

export const Route = createFileRoute('/games/$gameId')({
  component: RouteComponent,
  loader: async ({ params: { gameId } }): Promise<GameDetail> => {
    const res = await fetch(
      `${import.meta.env.VITE_API_URL}/api/games/${gameId}`,
    )
    if (!res.ok) {
      throw new Error('Game not found')
    }
    return res.json()
  },
})

function RouteComponent() {
  const game = Route.useLoaderData()
  const router = useRouter()

  useEffect(() => {
    document.title = `${game.title} — Checkpoint`
  }, [game.title])

  return (
    <div className="max-w-5xl mx-auto my-10">
      <button
        onClick={() => router.history.back()}
        className="text-muted-foreground hover:underline"
      >
        &larr; Back to catalog
      </button>

      <div className="mt-6 flex gap-8">
        {/* Cover */}
        {game.coverUrl ? (
          <img
            src={game.coverUrl}
            alt={game.title}
            className="w-64 h-auto rounded-lg object-cover shadow-md shrink-0"
          />
        ) : (
          <div className="w-64 h-80 rounded-lg bg-muted flex items-center justify-center text-muted-foreground shrink-0">
            No cover
          </div>
        )}

        {/* Info */}
        <div className="flex flex-col gap-4">
          <h1 className="text-3xl font-bold">{game.title}</h1>

          {game.releaseDate && (
            <p className="text-muted-foreground">
              Released:{' '}
              {new Date(game.releaseDate).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
          )}

          {game.averageRating != null && (
            <p className="text-muted-foreground">
              Rating: {game.averageRating.toFixed(1)} / 5 ({game.ratingCount}{' '}
              {game.ratingCount > 1 ? 'ratings' : 'rating'})
            </p>
          )}

          {game.genres.length > 0 && (
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-muted-foreground font-semibold">
                Genres:
              </span>
              {game.genres.map((g) => (
                <span
                  key={g.id}
                  className="px-2 py-0.5 rounded-full bg-muted text-sm"
                >
                  {g.name}
                </span>
              ))}
            </div>
          )}

          {game.platforms.length > 0 && (
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-muted-foreground font-semibold">
                Platforms:
              </span>
              {game.platforms.map((p) => (
                <span
                  key={p.id}
                  className="px-2 py-0.5 rounded-full bg-muted text-sm"
                >
                  {p.name}
                </span>
              ))}
            </div>
          )}

          {game.companies.length > 0 && (
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-muted-foreground font-semibold">
                Companies:
              </span>
              {game.companies.map((c) => (
                <span key={c.id} className="text-sm">
                  {c.name}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Description */}
      {game.description && (
        <>
          <Separator className="my-6" />
          <div>
            <h2 className="text-xl font-semibold mb-2">About</h2>
            <p className="text-muted-foreground leading-relaxed whitespace-pre-line">
              {game.description}
            </p>
          </div>
        </>
      )}
    </div>
  )
}
