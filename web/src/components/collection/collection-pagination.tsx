import type { CollectionTab } from '@/types/collection'
import { PaginationNav } from '@/components/shared/pagination-nav'

interface CollectionPaginationProps {
  tab: CollectionTab
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export function CollectionPagination({
  tab,
  page,
  totalPages,
  hasNext,
  hasPrevious,
}: CollectionPaginationProps) {
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
        search: { tab, page: target },
      })}
    />
  )
}
