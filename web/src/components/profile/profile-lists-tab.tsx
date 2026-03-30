import { useQuery } from '@tanstack/react-query'
import { List, Lock } from 'lucide-react'
import type { UserProfile } from '@/types/profile'
import { userListsQueryOptions } from '@/queries/lists'
import { ListsGrid } from '@/components/lists/lists-grid'

interface ProfileListsTabProps {
  profile: UserProfile
  page: number
}

export function ProfileListsTab({ profile, page }: ProfileListsTabProps) {
  const apiPage = Math.max(0, page - 1)
  const { data, isLoading, isError } = useQuery(
    userListsQueryOptions(profile.username, apiPage),
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
      <div className="grid grid-cols-1 gap-4 py-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="bg-muted h-64 animate-pulse rounded-lg" />
        ))}
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <List className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">Unable to load lists</p>
      </div>
    )
  }

  if (data.content.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <List className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">No lists yet</p>
      </div>
    )
  }

  return <ListsGrid lists={data.content} />
}
