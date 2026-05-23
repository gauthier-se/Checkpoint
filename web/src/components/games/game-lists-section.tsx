import { Link } from '@tanstack/react-router'
import { useSuspenseQuery } from '@tanstack/react-query'
import { ListCard } from '@/components/lists/list-card'
import { listsContainingGameQueryOptions } from '@/queries/lists'

interface GameListsSectionProps {
  gameId: string
}

export function GameListsSection({ gameId }: GameListsSectionProps) {
  const { data } = useSuspenseQuery(
    listsContainingGameQueryOptions(gameId, 0, 6),
  )
  const total = data.metadata.totalElements

  if (total === 0) {
    return null
  }

  return (
    <section className="mt-8">
      <h2 className="text-xl font-semibold mb-4">
        Appears in {total} {total === 1 ? 'list' : 'lists'}
      </h2>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {data.content.map((list) => (
          <ListCard key={list.id} list={list} />
        ))}
      </div>
      {total > 6 && (
        <div className="mt-4 text-right">
          <Link
            to="/games/$gameId/lists"
            params={{ gameId }}
            search={{ page: 1 }}
            className="text-sm font-medium text-primary hover:underline"
          >
            See all {total} lists →
          </Link>
        </div>
      )}
    </section>
  )
}
