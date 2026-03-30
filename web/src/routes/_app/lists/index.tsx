import { createFileRoute, Link } from '@tanstack/react-router'
import { useSuspenseQuery } from '@tanstack/react-query'
import { List, Plus } from 'lucide-react'
import type { GameListsResponse } from '@/types/list'
import {
  popularListsQueryOptions,
  recentListsQueryOptions,
} from '@/queries/lists'
import { ListsGrid } from '@/components/lists/lists-grid'
import { ListsPagination } from '@/components/lists/lists-pagination'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { useAuth } from '@/hooks/use-auth'
import { apiFetch } from '@/services/api'

type ListsSearchParams = {
  page: number
}

const PAGE_SIZE = 20

export const Route = createFileRoute('/_app/lists/')({
  component: RouteComponent,
  validateSearch: (search: Record<string, unknown>): ListsSearchParams => ({
    page: Math.max(1, Math.floor(Number(search.page ?? 1)) || 1),
  }),
  loaderDeps: ({ search }) => search,
  loader: async ({ deps, context }): Promise<GameListsResponse> => {
    const apiPage = Math.max(0, deps.page - 1)
    const [data] = await Promise.all([
      apiFetch(`/api/lists?page=${apiPage}&size=${PAGE_SIZE}`).then(
        (res): Promise<GameListsResponse> => res.json(),
      ),
      context.queryClient.ensureQueryData(popularListsQueryOptions(0, 5)),
    ])
    return data
  },
})

function ListSection({
  title,
  action,
  children,
}: {
  title: string
  action?: React.ReactNode
  children: React.ReactNode
}) {
  return (
    <div className="my-8">
      <div className="flex items-center justify-between py-2">
        <h2 className="text-muted-foreground font-semibold">{title}</h2>
        {action}
      </div>
      <Separator />
      {children}
    </div>
  )
}

function RouteComponent() {
  const data = Route.useLoaderData()
  const searchParams = Route.useSearch()
  const { page } = searchParams
  const { user } = useAuth()

  const { data: popularLists } = useSuspenseQuery(
    popularListsQueryOptions(0, 5),
  )

  return (
    <div className="max-w-7xl mx-auto">
      <div className="mt-10 flex items-center justify-between">
        <h1 className="text-xl font-bold">Lists</h1>
        {user && (
          <Button asChild size="sm">
            <Link to="/lists">
              <Plus />
              Create a list
            </Link>
          </Button>
        )}
      </div>

      {popularLists.content.length > 0 && (
        <ListSection title="Popular Lists">
          <ListsGrid lists={popularLists.content} />
        </ListSection>
      )}

      <ListSection title="Recent Lists">
        {data.content.length > 0 ? (
          <>
            <ListsGrid lists={data.content} />
            <ListsPagination
              page={page}
              totalPages={data.metadata.totalPages}
              hasNext={data.metadata.hasNext}
              hasPrevious={data.metadata.hasPrevious}
              search={searchParams}
            />
          </>
        ) : (
          <div className="flex flex-col items-center gap-3 py-12 text-center">
            <List className="text-muted-foreground size-12" />
            <p className="text-muted-foreground text-lg">No lists yet</p>
          </div>
        )}
      </ListSection>
    </div>
  )
}
