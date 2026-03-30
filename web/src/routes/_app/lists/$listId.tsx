import { createFileRoute, Link } from '@tanstack/react-router'
import { useSuspenseQuery } from '@tanstack/react-query'
import { ArrowLeft, Gamepad2, Heart, Lock } from 'lucide-react'
import { listDetailQueryOptions } from '@/queries/lists'
import { ListGameItem } from '@/components/lists/list-game-item'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'

export const Route = createFileRoute('/_app/lists/$listId')({
  component: RouteComponent,
  loader: async ({ params: { listId }, context }) => {
    await context.queryClient.ensureQueryData(listDetailQueryOptions(listId))
  },
})

function RouteComponent() {
  const { listId } = Route.useParams()
  const { data: list } = useSuspenseQuery(listDetailQueryOptions(listId))

  const initials = list.authorPseudo.slice(0, 2).toUpperCase()
  const createdDate = new Date(list.createdAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })

  return (
    <main className="mx-auto max-w-3xl px-4 py-10">
      <Link
        to="/lists"
        search={{ page: 1 }}
        className="mb-6 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="size-4" />
        Back to lists
      </Link>

      <div className="space-y-4">
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <h1 className="text-3xl font-bold">{list.title}</h1>
              {list.isPrivate && (
                <span className="flex items-center gap-1 rounded-full bg-muted px-2.5 py-0.5 text-xs font-medium text-muted-foreground">
                  <Lock className="size-3" />
                  Private
                </span>
              )}
            </div>
            {list.description && (
              <p className="text-muted-foreground max-w-prose">
                {list.description}
              </p>
            )}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <Link
            to="/profile/$username"
            params={{ username: list.authorPseudo }}
            className="flex items-center gap-2 hover:underline"
          >
            <Avatar className="size-6">
              <AvatarImage
                src={list.authorPicture ?? undefined}
                alt={list.authorPseudo}
              />
              <AvatarFallback className="text-[10px]">
                {initials}
              </AvatarFallback>
            </Avatar>
            <span className="text-sm font-medium">{list.authorPseudo}</span>
          </Link>
          <span className="text-sm text-muted-foreground">{createdDate}</span>
        </div>

        <div className="flex items-center gap-4 text-sm text-muted-foreground">
          <span className="flex items-center gap-1">
            <Gamepad2 className="size-4" />
            {list.videoGamesCount}{' '}
            {list.videoGamesCount === 1 ? 'game' : 'games'}
          </span>
          <Button variant="ghost" size="sm" className="gap-1" disabled>
            <Heart
              className={`size-4 ${list.hasLiked ? 'fill-current text-red-500' : ''}`}
            />
            {list.likesCount}
          </Button>
        </div>
      </div>

      <Separator className="my-6" />

      {list.entries.length > 0 ? (
        <div className="space-y-2">
          {list.entries.map((entry) => (
            <ListGameItem key={entry.videoGameId} entry={entry} />
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center gap-3 py-12 text-center">
          <Gamepad2 className="text-muted-foreground size-12" />
          <p className="text-muted-foreground text-lg">
            This list has no games yet
          </p>
        </div>
      )}
    </main>
  )
}
