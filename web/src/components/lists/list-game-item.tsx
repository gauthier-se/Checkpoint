import { Link } from '@tanstack/react-router'
import type { GameListEntry } from '@/types/list'

interface ListGameItemProps {
  entry: GameListEntry
}

export function ListGameItem({ entry }: ListGameItemProps) {
  return (
    <Link
      to="/games/$gameId"
      params={{ gameId: entry.videoGameId }}
      className="flex items-center gap-4 rounded-lg border p-3 transition-colors hover:bg-accent"
    >
      <span className="w-8 text-center text-lg font-bold text-muted-foreground">
        {entry.position + 1}
      </span>
      <img
        src={entry.coverUrl}
        alt={entry.title}
        className="h-16 w-12 rounded-sm object-cover"
      />
      <div className="flex-1 min-w-0">
        <h4 className="font-semibold leading-tight truncate">{entry.title}</h4>
        {entry.releaseDate && (
          <p className="text-sm text-muted-foreground">
            {new Date(entry.releaseDate).getFullYear()}
          </p>
        )}
      </div>
    </Link>
  )
}
