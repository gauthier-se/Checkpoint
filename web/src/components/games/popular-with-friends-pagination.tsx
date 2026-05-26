import { PaginationNav } from '@/components/shared/pagination-nav'

interface PopularWithFriendsPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export function PopularWithFriendsPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
}: PopularWithFriendsPaginationProps) {
  return (
    <PaginationNav
      page={page}
      totalPages={totalPages}
      hasNext={hasNext}
      hasPrevious={hasPrevious}
      className="mt-6 mb-10"
      linkProps={(target) => ({
        to: '/games/popular-with-friends',
        search: { page: target },
      })}
    />
  )
}
