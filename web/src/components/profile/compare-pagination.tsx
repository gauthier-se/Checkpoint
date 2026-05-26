import { PaginationNav } from '@/components/shared/pagination-nav'

interface ComparePaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

/**
 * Pagination for the profile comparison common-games list. Uses relative ("." )
 * navigation so it stays bound to the current compare route.
 */
export function ComparePagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
}: ComparePaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      hideWhenSinglePage
      className="pt-6 pb-4"
      linkProps={(target) => ({
        to: '.',
        search: { page: target },
      })}
    />
  )
}
