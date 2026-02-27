import {
  queryOptions,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import {
  BookOpen,
  Calendar,
  Clock,
  Gamepad2,
  RefreshCw,
  Trash2,
} from 'lucide-react'
import type { PlayLogListResponse, PlayStatus } from '@/types/collection'
import { CollectionPagination } from '@/components/collection/collection-pagination'
import { EmptyState } from '@/components/collection/empty-state'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { apiFetch } from '@/services/api'

const PAGE_SIZE = 20

export function playLogQuery(page: number) {
  return queryOptions({
    queryKey: ['plays', 'me', page],
    queryFn: async (): Promise<PlayLogListResponse> => {
      const apiPage = page - 1
      const res = await apiFetch(
        `/api/me/plays?page=${apiPage}&size=${PAGE_SIZE}&sort=updatedAt,desc`,
      )
      if (!res.ok) throw new Error('Failed to load play log')
      return res.json()
    },
  })
}

const PLAY_STATUS_LABELS: Record<PlayStatus, string> = {
  ARE_PLAYING: 'Playing',
  PLAYED: 'Played',
  COMPLETED: 'Completed',
  RETIRED: 'Retired',
  SHELVED: 'Shelved',
  ABANDONED: 'Abandoned',
}

const PLAY_STATUS_COLORS: Record<PlayStatus, string> = {
  ARE_PLAYING: 'bg-blue-500/15 text-blue-400 border-blue-500/20',
  PLAYED: 'bg-violet-500/15 text-violet-400 border-violet-500/20',
  COMPLETED: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/20',
  RETIRED: 'bg-slate-500/15 text-slate-400 border-slate-500/20',
  SHELVED: 'bg-amber-500/15 text-amber-400 border-amber-500/20',
  ABANDONED: 'bg-red-500/15 text-red-400 border-red-500/20',
}

function formatTimePlayed(minutes: number): string {
  if (minutes < 60) return `${minutes}m`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

interface PlayLogTabProps {
  page: number
}

export function PlayLogTab({ page }: PlayLogTabProps) {
  const { data, isLoading, isError } = useQuery(playLogQuery(page))
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: async (playId: string) => {
      const res = await apiFetch(`/api/me/plays/${playId}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Failed to delete play log')
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['plays', 'me'] })
    },
  })

  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="flex items-start gap-4 rounded-lg border p-4">
            <div className="h-24 w-16 animate-pulse rounded-md bg-muted" />
            <div className="flex-1 space-y-2">
              <div className="h-5 w-1/3 animate-pulse rounded bg-muted" />
              <div className="h-4 w-1/2 animate-pulse rounded bg-muted" />
            </div>
          </div>
        ))}
      </div>
    )
  }

  if (isError || !data) {
    return (
      <EmptyState
        icon={<BookOpen className="size-12" />}
        title="Unable to load play log"
        description="The play log feature is not available yet. Check back soon!"
      />
    )
  }

  if (data.content.length === 0) {
    return (
      <EmptyState
        icon={<BookOpen className="size-12" />}
        title="Your play log is empty"
        description="Start logging your play sessions to build a diary of your gaming journey!"
        actionLabel="Browse Games"
        actionTo="/games"
      />
    )
  }

  return (
    <div>
      <div className="space-y-3">
        {data.content.map((entry) => (
          <div
            key={entry.id}
            className="group flex items-start gap-4 rounded-lg border bg-card p-4 shadow-sm transition-shadow hover:shadow-md"
          >
            {/* Cover */}
            <Link
              to="/games/$gameId"
              params={{ gameId: entry.videoGameId }}
              className="shrink-0"
            >
              <div className="h-24 w-16 overflow-hidden rounded-md bg-muted">
                {entry.coverUrl ? (
                  <img
                    src={entry.coverUrl}
                    alt={entry.title}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center bg-secondary">
                    <Gamepad2 className="size-4 text-muted-foreground" />
                  </div>
                )}
              </div>
            </Link>

            {/* Info */}
            <div className="flex min-w-0 flex-1 flex-col gap-1.5">
              <div className="flex items-start justify-between gap-2">
                <Link
                  to="/games/$gameId"
                  params={{ gameId: entry.videoGameId }}
                  className="font-semibold leading-tight hover:underline line-clamp-1"
                >
                  {entry.title}
                </Link>
                <div className="flex shrink-0 items-center gap-1.5">
                  <Badge
                    className={`${PLAY_STATUS_COLORS[entry.status]} text-[11px]`}
                  >
                    {PLAY_STATUS_LABELS[entry.status]}
                  </Badge>
                  {entry.isReplay && (
                    <Badge variant="outline" className="gap-1 text-[11px]">
                      <RefreshCw className="size-2.5" />
                      Replay
                    </Badge>
                  )}
                </div>
              </div>

              {/* Metadata row */}
              <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
                {entry.platformName && (
                  <span className="flex items-center gap-1">
                    <Gamepad2 className="size-3" />
                    {entry.platformName}
                  </span>
                )}
                {entry.timePlayed != null && entry.timePlayed > 0 && (
                  <span className="flex items-center gap-1">
                    <Clock className="size-3" />
                    {formatTimePlayed(entry.timePlayed)}
                  </span>
                )}
                {entry.startDate && (
                  <span className="flex items-center gap-1">
                    <Calendar className="size-3" />
                    {formatDate(entry.startDate)}
                    {entry.endDate && ` — ${formatDate(entry.endDate)}`}
                  </span>
                )}
              </div>
            </div>

            {/* Delete action */}
            <Button
              variant="ghost"
              size="sm"
              className="h-7 shrink-0 gap-1 text-xs text-destructive opacity-0 transition-opacity hover:text-destructive group-hover:opacity-100"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(entry.id)}
            >
              <Trash2 className="size-3" />
              Delete
            </Button>
          </div>
        ))}
      </div>
      <CollectionPagination
        tab="playlog"
        page={page}
        totalPages={data.metadata.totalPages}
        hasNext={data.metadata.hasNext}
        hasPrevious={data.metadata.hasPrevious}
      />
    </div>
  )
}
