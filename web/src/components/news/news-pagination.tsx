import type { NewsListSearchParams } from '@/types/news'
import { PaginationNav } from '@/components/shared/pagination-nav'

interface NewsPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  search: NewsListSearchParams
}

export function NewsPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
  search,
}: NewsPaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      className="mt-6 mb-10"
      linkProps={(target) => ({
        to: '/news',
        search: { ...search, page: target },
      })}
    />
  )
}
