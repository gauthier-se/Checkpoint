import { ListCard } from './list-card'
import type { GameListCard } from '@/types/list'

interface ListsGridProps {
  lists: Array<GameListCard>
}

export function ListsGrid({ lists }: ListsGridProps) {
  return (
    <div className="grid grid-cols-1 gap-4 py-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
      {lists.map((list) => (
        <ListCard key={list.id} list={list} />
      ))}
    </div>
  )
}
