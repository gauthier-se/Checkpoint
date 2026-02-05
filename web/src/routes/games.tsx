import { Button } from '@/components/ui/button'
import { ButtonGroup } from '@/components/ui/button-group'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { createFileRoute, Link } from '@tanstack/react-router'

interface Game {
  id: string
  title: string
  coverUrl: string
  releaseDate: string
  averageRating: number | null
  ratingCount: number
}

interface PaginationMetadata {
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
}

interface GamesResponse {
  content: Game[]
  metadata: PaginationMetadata
}

export const Route = createFileRoute('/games')({
  component: RouteComponent,
  loader: async (): Promise<GamesResponse> => {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/games`)
    const data: GamesResponse = await res.json()
    return data
  },
})

function RouteComponent() {
  const data = Route.useLoaderData()

  return (
    <div className="max-w-7xl mx-auto">
      <div className="mt-10 py-2 text-muted-foreground font-semibold flex items-center justify-between">
        <div className="flex items-center gap-4">
          <p>Browse by</p>
          <ButtonGroup>
            <Button variant="outline">Year</Button>
            <Button variant="outline">Rating</Button>
            <Button variant="outline">Genre</Button>
            <Button variant="outline">Platform</Button>
            <Button variant="outline">Other</Button>
          </ButtonGroup>
        </div>
        <div className="flex items-center gap-4">
          <p className="min-w-fit">Find a game</p>
          <Input />
        </div>
      </div>
      <div className="my-8">
        <div className="py-2 text-muted-foreground font-semibold flex items-center justify-between">
          <h2>Popular games this week</h2>
          <Link to="/games">More</Link>
        </div>
        <Separator />
        <div className="grid grid-cols-7 gap-3 py-4">
          {data.content.slice(0, 7).map((game) => (
            <div key={game.id}>
              <img
                className="rounded-sm w-full"
                src={game.coverUrl}
                alt={game.title}
              />
            </div>
          ))}
        </div>
      </div>
      <h2 className="py-2 text-muted-foreground font-semibold">
        There {data.metadata.totalElements > 1 ? 'are' : 'is'}{' '}
        {data.metadata.totalElements} game
        {data.metadata.totalElements > 1 ? 's' : ''}
      </h2>
      <Separator />
      <div className="grid grid-cols-8 gap-2 py-4">
        {data.content.map((game) => (
          <div key={game.id}>
            <img
              className="rounded-sm w-full"
              src={game.coverUrl}
              alt={game.title}
            />
          </div>
        ))}
      </div>
      <pre>
        <code>{JSON.stringify(data, null, 2)}</code>
      </pre>
    </div>
  )
}
