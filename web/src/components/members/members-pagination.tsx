import { PaginationNav } from '@/components/shared/pagination-nav'

interface MembersPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  search: Record<string, unknown>
}

export function MembersPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
  search,
}: MembersPaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      className="mt-6 mb-10"
      linkProps={(target) => ({
        to: '/members/all',
        search: { ...search, page: target },
      })}
    />
  )
}
