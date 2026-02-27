import { Link } from '@tanstack/react-router'

interface CollectionGameCardProps {
  videoGameId: string
  title: string
  coverUrl: string | null
  children?: React.ReactNode
}

export function CollectionGameCard({
  videoGameId,
  title,
  coverUrl,
  children,
}: CollectionGameCardProps) {
  return (
    <div className="group relative flex flex-col gap-2 rounded-lg border bg-card p-3 text-card-foreground shadow-sm transition-shadow hover:shadow-md">
      <Link
        to="/games/$gameId"
        params={{ gameId: videoGameId }}
        className="block"
      >
        <div className="aspect-[3/4] overflow-hidden rounded-md bg-muted">
          {coverUrl ? (
            <img
              src={coverUrl}
              alt={title}
              className="h-full w-full object-cover transition-transform duration-200 group-hover:scale-105"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-secondary">
              <span className="text-xs text-muted-foreground">No Cover</span>
            </div>
          )}
        </div>
      </Link>
      <h3 className="text-sm font-semibold leading-tight line-clamp-2">
        {title}
      </h3>
      {children}
    </div>
  )
}
