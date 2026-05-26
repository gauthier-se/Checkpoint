import { PaginationNav } from '@/components/shared/pagination-nav'

const CATALOG_HASH = 'catalog'

interface GamesPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  search: Record<string, unknown>
}

export function GamesPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
  search,
}: GamesPaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      className="mt-6 mb-10"
      linkProps={(target) => ({
        to: '/games/filtered',
        search: { ...search, page: target },
        hash: CATALOG_HASH,
      })}
    />
  )
}
