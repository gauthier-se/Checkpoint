import { useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { Lock, Tag } from 'lucide-react'
import type { UserProfile } from '@/types/profile'
import { Badge } from '@/components/ui/badge'
import { userTagsQueryOptions } from '@/queries/tags'

interface ProfileTagsTabProps {
  profile: UserProfile
}

export function ProfileTagsTab({ profile }: ProfileTagsTabProps) {
  const {
    data: tags,
    isLoading,
    isError,
  } = useQuery(userTagsQueryOptions(profile.username))

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
      <div className="flex flex-wrap gap-2 py-4">
        {Array.from({ length: 6 }).map((_, i) => (
          <div
            key={i}
            className="h-8 w-24 animate-pulse rounded-full bg-muted"
          />
        ))}
      </div>
    )
  }

  if (isError || !tags) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Tag className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">Unable to load tags</p>
      </div>
    )
  }

  if (tags.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <Tag className="text-muted-foreground size-12" />
        <p className="text-muted-foreground text-lg">No tags yet</p>
      </div>
    )
  }

  return (
    <div className="flex flex-wrap gap-2">
      {tags.map((tag) => (
        <Link
          key={tag.id}
          to="/profile/$username/tags/$tagName"
          params={{ username: profile.username, tagName: tag.name }}
        >
          <Badge
            variant="secondary"
            className="gap-1.5 px-3 py-1.5 text-sm hover:bg-secondary/80 transition-colors cursor-pointer"
          >
            <Tag className="size-3.5" />
            {tag.name}
            <span className="text-muted-foreground">{tag.playLogsCount}</span>
          </Badge>
        </Link>
      ))}
    </div>
  )
}
