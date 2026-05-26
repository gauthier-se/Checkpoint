import { PaginationNav } from '@/components/shared/pagination-nav'

interface FeedPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export function FeedPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
}: FeedPaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      className="mt-6 mb-10"
      linkProps={(target) => ({
        to: '/feed',
        search: (prev) => ({ ...prev, page: target }),
      })}
    />
  )
}
