import { PaginationNav } from '@/components/shared/pagination-nav'

interface ListsPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  search: Record<string, unknown>
}

export function ListsPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
  search,
}: ListsPaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      className="mt-6 mb-10"
      linkProps={(target) => ({
        to: '/lists/browse',
        search: { ...search, page: target },
      })}
    />
  )
}
