import { useEffect } from 'react'

import { useQuery } from '@tanstack/react-query'
import { Link, createFileRoute, useRouter } from '@tanstack/react-router'
import { Star } from 'lucide-react'
import type { GameDetail } from '@/types/game'
import { ReviewForm } from '@/components/reviews/review-form'
import { ReviewList } from '@/components/reviews/review-list'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { useAuth } from '@/hooks/use-auth'
import {
  gameReviewsQueryOptions,
  userReviewQueryOptions,
} from '@/queries/review'
import { apiFetch } from '@/services/api'

export const Route = createFileRoute('/_app/games/$gameId')({
  component: RouteComponent,
  loader: async ({ params: { gameId }, context }) => {
    // start fetching reviews in background
    void context.queryClient.prefetchQuery(
      gameReviewsQueryOptions(gameId, 0, 10),
    )

    const res = await apiFetch(`/api/games/${gameId}`)
    if (!res.ok) {
      throw new Error('Game not found')
    }
    return res.json() as Promise<GameDetail>
  },
})

function RouteComponent() {
  const game = Route.useLoaderData()
  const router = useRouter()
  const { user } = useAuth()

  // Fetch user's existing review if authenticated
  const { data: userReview } = useQuery({
    ...userReviewQueryOptions(game.id),
    enabled: !!user,
  })

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
            <div className="flex items-center gap-2 mt-1">
              <div className="flex items-center bg-yellow-400/10 text-yellow-600 px-3 py-1.5 rounded-md">
                <Star className="h-5 w-5 fill-yellow-400 text-yellow-500 mr-2" />
                <span className="text-xl font-bold font-mono">
                  {game.averageRating.toFixed(1)}
                </span>
                <span className="text-muted-foreground ml-1 text-sm">/ 5</span>
              </div>
              <span className="text-muted-foreground text-sm">
                ({game.ratingCount}{' '}
                {game.ratingCount > 1 ? 'ratings' : 'rating'})
              </span>
            </div>
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

      {game.description && (
        <>
          <Separator className="my-6" />
          <div className="mb-10">
            <h2 className="text-xl font-semibold mb-2">About</h2>
            <p className="text-muted-foreground leading-relaxed whitespace-pre-line">
              {game.description}
            </p>
          </div>
        </>
      )}

      <Separator className="my-6" />

      {/* Review Form Section */}
      <div className="mb-10">
        {!user ? (
          <div className="bg-muted p-6 rounded-lg border text-center flex flex-col items-center justify-center gap-4">
            <h3 className="text-lg font-semibold">Write a Review</h3>
            <p className="text-muted-foreground">
              Log in to leave a review and a rating for {game.title}.
            </p>
            <Button asChild>
              <Link to="/login" search={{ redirect: `/games/${game.id}` }}>
                Log in to leave a review
              </Link>
            </Button>
          </div>
        ) : (
          <ReviewForm gameId={game.id} existingReview={userReview ?? null} />
        )}
      </div>

      {/* Reviews List */}
      <ReviewList gameId={game.id} />
    </div>
  )
}
