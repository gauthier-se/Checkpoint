import { GameGrid } from '@/components/games/game-grid'
import { GamesPagination } from '@/components/games/pagination'
import { Button } from '@/components/ui/button'
import { ButtonGroup } from '@/components/ui/button-group'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import type { GamesResponse } from '@/types/game'
import { createFileRoute, Link } from '@tanstack/react-router'

const PAGE_SIZE = 32

type GamesSearchParams = {
  page: number
}

export const Route = createFileRoute('/games/')({
  component: RouteComponent,
  validateSearch: (search: Record<string, unknown>): GamesSearchParams => ({
    page: Math.max(1, Math.floor(Number(search?.page ?? 1)) || 1),
  }),
  loaderDeps: ({ search: { page } }) => ({ page }),
  loader: async ({ deps: { page } }): Promise<GamesResponse> => {
    const apiPage = page - 1 // API is 0-based, URL is 1-based
    const res = await fetch(
      `${import.meta.env.VITE_API_URL}/api/games?page=${apiPage}&size=${PAGE_SIZE}`,
    )
    const data: GamesResponse = await res.json()
    return data
  },
})

function RouteComponent() {
  const data = Route.useLoaderData()
  const { page } = Route.useSearch()

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
          <Link to="/games" search={{ page: 1 }}>
            More
          </Link>
        </div>
        <Separator />
        {/* TODO: API should support "featured" or "popular" games so we don't have to slice here */}
        <GameGrid games={data.content.slice(0, 7)} columns={7} />
      </div>
      <h2 id="catalog" className="py-2 text-muted-foreground font-semibold">
        There {data.metadata.totalElements > 1 ? 'are' : 'is'}{' '}
        {data.metadata.totalElements} game
        {data.metadata.totalElements > 1 ? 's' : ''}
      </h2>
      <Separator />
      <GameGrid games={data.content} />
      <GamesPagination
        page={page}
        totalPages={data.metadata.totalPages}
        hasNext={data.metadata.hasNext}
        hasPrevious={data.metadata.hasPrevious}
      />
    </div>
  )
}
