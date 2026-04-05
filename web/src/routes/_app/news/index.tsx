import { createFileRoute } from '@tanstack/react-router'
import { Newspaper } from 'lucide-react'
import type { NewsResponse } from '@/types/news'
import { NewsCard } from '@/components/news/news-card'
import { NewsPagination } from '@/components/news/news-pagination'
import { Separator } from '@/components/ui/separator'
import { apiFetch } from '@/services/api'

type NewsSearchParams = {
  page: number
}

const PAGE_SIZE = 12

export const Route = createFileRoute('/_app/news/')({
  component: RouteComponent,
  validateSearch: (search: Record<string, unknown>): NewsSearchParams => ({
    page: Math.max(1, Math.floor(Number(search.page ?? 1)) || 1),
  }),
  loaderDeps: ({ search }) => search,
  loader: async ({ deps }): Promise<NewsResponse> => {
    const apiPage = Math.max(0, deps.page - 1)
    const res = await apiFetch(`/api/news?page=${apiPage}&size=${PAGE_SIZE}`)
    if (!res.ok) throw new Error('Failed to fetch news')
    return res.json()
  },
})

function RouteComponent() {
  const data = Route.useLoaderData()
  const searchParams = Route.useSearch()
  const { page } = searchParams

  return (
    <div className="max-w-7xl mx-auto">
      <div className="mt-10">
        <h1 className="text-xl font-bold">News</h1>
      </div>

      <div className="my-8">
        <div className="py-2">
          <h2 className="text-muted-foreground font-semibold">
            Latest articles
          </h2>
        </div>
        <Separator />

        {data.content.length > 0 ? (
          <>
            <div className="grid grid-cols-1 gap-4 py-4 sm:grid-cols-2 lg:grid-cols-3">
              {data.content.map((article) => (
                <NewsCard key={article.id} article={article} />
              ))}
            </div>
            <NewsPagination
              page={page}
              totalPages={data.metadata.totalPages}
              hasNext={data.metadata.hasNext}
              hasPrevious={data.metadata.hasPrevious}
              search={searchParams}
            />
          </>
        ) : (
          <div className="flex flex-col items-center gap-3 py-12 text-center">
            <Newspaper className="text-muted-foreground size-12" />
            <p className="text-muted-foreground text-lg">No news yet</p>
          </div>
        )}
      </div>
    </div>
  )
}
