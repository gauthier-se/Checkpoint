import { useDeferredValue, useEffect, useState } from 'react'
import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { queryOptions, useQuery } from '@tanstack/react-query'
import { Loader2, Search, X } from 'lucide-react'
import type { Game, GamesResponse } from '@/types/game'
import { GameGrid } from '@/components/games/game-grid'
import { GamesPagination } from '@/components/games/pagination'
import { Button } from '@/components/ui/button'
import { ButtonGroup } from '@/components/ui/button-group'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { apiFetch } from '@/services/api'

const PAGE_SIZE = 32

type GamesSearchParams = {
  page: number
  q?: string
}

function searchGamesQuery(q: string) {
  return queryOptions({
    queryKey: ['games', 'search', q],
    queryFn: async (): Promise<Array<Game>> => {
      const res = await apiFetch(`/api/games/search?q=${encodeURIComponent(q)}`)
      if (!res.ok) throw new Error('Failed to search games')
      return res.json()
    },
  })
}

export const Route = createFileRoute('/_app/games/')({
  component: RouteComponent,
  validateSearch: (search: Record<string, unknown>): GamesSearchParams => ({
    page: Math.max(1, Math.floor(Number(search.page ?? 1)) || 1),
    q:
      typeof search.q === 'string' && search.q.length > 0
        ? search.q
        : undefined,
  }),
  loaderDeps: ({ search: { page } }) => ({ page }),
  loader: async ({ deps: { page } }): Promise<GamesResponse> => {
    const apiPage = page - 1 // API is 0-based, URL is 1-based
    const res = await apiFetch(`/api/games?page=${apiPage}&size=${PAGE_SIZE}`)
    const data: GamesResponse = await res.json()
    return data
  },
})

function RouteComponent() {
  const data = Route.useLoaderData()
  const { page, q } = Route.useSearch()
  const navigate = useNavigate({ from: '/games' })

  const [inputValue, setInputValue] = useState(q ?? '')
  const deferredQuery = useDeferredValue(inputValue)

  const isSearchActive = deferredQuery.length >= 2

  const {
    data: searchResults,
    isLoading: isSearchLoading,
    isFetching: isSearchFetching,
  } = useQuery({
    ...searchGamesQuery(deferredQuery),
    enabled: isSearchActive,
  })

  // Sync deferred query to URL
  useEffect(() => {
    const urlQ = deferredQuery.length >= 2 ? deferredQuery : undefined
    if (urlQ !== q) {
      navigate({
        search: (prev) => ({ ...prev, q: urlQ }),
        replace: true,
      })
    }
  }, [deferredQuery, q, navigate])

  // Sync URL q to input when navigating directly (e.g. shared link)
  useEffect(() => {
    if (q && q !== inputValue) {
      setInputValue(q)
    }
  }, [q])  

  function clearSearch() {
    setInputValue('')
    navigate({
      search: (prev) => ({ ...prev, q: undefined }),
      replace: true,
    })
  }

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
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
            <Input
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder="Search..."
              className="pl-8 pr-8"
            />
            {inputValue.length > 0 && (
              <button
                type="button"
                onClick={clearSearch}
                className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="size-4" />
              </button>
            )}
          </div>
        </div>
      </div>

      {isSearchActive ? (
        <div className="my-8">
          <h2 className="py-2 text-muted-foreground font-semibold">
            {isSearchLoading
              ? 'Searching...'
              : searchResults && searchResults.length > 0
                ? `${searchResults.length} result${searchResults.length > 1 ? 's' : ''} for "${deferredQuery}"`
                : `No games found for "${deferredQuery}"`}
          </h2>
          <Separator />
          {isSearchLoading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="size-6 animate-spin text-muted-foreground" />
            </div>
          ) : searchResults && searchResults.length > 0 ? (
            <div className="relative">
              {isSearchFetching && (
                <div className="absolute inset-0 bg-background/50 flex justify-center pt-12 z-10">
                  <Loader2 className="size-6 animate-spin text-muted-foreground" />
                </div>
              )}
              <GameGrid games={searchResults} />
            </div>
          ) : (
            <p className="py-8 text-center text-muted-foreground">
              No games to display.
            </p>
          )}
        </div>
      ) : (
        <>
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
            {data.metadata.totalElements === 0
              ? 'No games found'
              : `There ${data.metadata.totalElements > 1 ? 'are' : 'is'} ${data.metadata.totalElements} game${data.metadata.totalElements > 1 ? 's' : ''}`}
          </h2>
          <Separator />
          {data.content.length > 0 ? (
            <>
              <GameGrid games={data.content} />
              <GamesPagination
                page={page}
                totalPages={data.metadata.totalPages}
                hasNext={data.metadata.hasNext}
                hasPrevious={data.metadata.hasPrevious}
              />
            </>
          ) : (
            <p className="py-8 text-center text-muted-foreground">
              No games to display.
            </p>
          )}
        </>
      )}
    </div>
  )
}
