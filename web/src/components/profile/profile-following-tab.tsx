import { useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { Users } from 'lucide-react'
import type { UserProfile } from '@/types/profile'
import { userFollowingQueryOptions } from '@/queries/profile'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'

interface ProfileFollowingTabProps {
  profile: UserProfile
  page: number
}

export function ProfileFollowingTab({
  profile,
  page,
}: ProfileFollowingTabProps) {
  const apiPage = Math.max(0, page - 1)
  const { data, isLoading, isError } = useQuery(
    userFollowingQueryOptions(profile.id, apiPage),
  )

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-3 rounded-lg border p-4"
          >
            <div className="bg-muted size-10 animate-pulse rounded-full" />
            <div className="bg-muted h-4 w-24 animate-pulse rounded" />
          </div>
        ))}
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Users className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">
          Unable to load following list
        </p>
      </div>
    )
  }

  if (data.content.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Users className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">
          Not following anyone yet
        </p>
      </div>
    )
  }

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
      {data.content.map((user) => (
        <Link
          key={user.id}
          to="/profile/$username"
          params={{ username: user.pseudo }}
          className="flex items-center gap-3 rounded-lg border p-4 transition-colors hover:bg-accent"
        >
          <Avatar className="size-10">
            <AvatarImage src={user.picture ?? undefined} alt={user.pseudo} />
            <AvatarFallback>
              {user.pseudo.slice(0, 2).toUpperCase()}
            </AvatarFallback>
          </Avatar>
          <span className="truncate font-medium">{user.pseudo}</span>
        </Link>
      ))}
    </div>
  )
}
