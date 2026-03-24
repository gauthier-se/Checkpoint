import { useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { Heart, Lock } from 'lucide-react'
import { userWishlistQueryOptions } from '@/queries/profile'
import type { UserProfile } from '@/types/profile'

interface ProfileWishlistTabProps {
  profile: UserProfile
  page: number
}

export function ProfileWishlistTab({ profile, page }: ProfileWishlistTabProps) {
  const apiPage = Math.max(0, page - 1)
  const { data, isLoading, isError } = useQuery(
    userWishlistQueryOptions(profile.username, apiPage),
  )

  if (profile.isPrivate && !profile.isOwner) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Lock className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">This profile is private</p>
      </div>
    )
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        {Array.from({ length: 10 }).map((_, i) => (
          <div key={i} className="flex flex-col gap-2 rounded-lg border p-3">
            <div className="bg-muted aspect-[3/4] animate-pulse rounded-md" />
            <div className="bg-muted h-4 w-3/4 animate-pulse rounded" />
          </div>
        ))}
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Heart className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">Unable to load wishlist</p>
      </div>
    )
  }

  if (data.content.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Heart className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">No games in wishlist</p>
      </div>
    )
  }

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
      {data.content.map((game) => (
        <Link
          key={game.id}
          to="/games/$gameId"
          params={{ gameId: game.videoGameId }}
          className="group flex flex-col gap-2 rounded-lg border p-3 transition-colors hover:bg-accent"
        >
          {game.coverUrl ? (
            <img
              src={game.coverUrl}
              alt={game.title}
              className="aspect-[3/4] rounded-md object-cover"
            />
          ) : (
            <div className="bg-muted flex aspect-[3/4] items-center justify-center rounded-md">
              <Heart className="text-muted-foreground size-8" />
            </div>
          )}
          <p className="text-sm font-medium leading-tight">{game.title}</p>
          {game.releaseDate && (
            <p className="text-muted-foreground text-xs">
              {new Date(game.releaseDate).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
              })}
            </p>
          )}
        </Link>
      ))}
    </div>
  )
}
