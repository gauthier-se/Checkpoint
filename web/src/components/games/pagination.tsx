import { Link } from '@tanstack/react-router'
import { ArrowLeft, ArrowRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ButtonGroup } from '@/components/ui/button-group'
import { getPageNumbers } from '@/lib/pagination'

const CATALOG_HASH = 'catalog'

interface GamesPaginationProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export function GamesPagination({
  page,
  totalPages,
  hasNext,
  hasPrevious,
}: GamesPaginationProps) {
  return (
    <div className="flex items-center justify-between mt-6 mb-10">
      <Link
        to="/games"
        search={{ page: page - 1 }}
        hash={CATALOG_HASH}
        disabled={!hasPrevious}
      >
        <Button variant="outline" disabled={!hasPrevious}>
          <ArrowLeft />
          Previous
        </Button>
      </Link>
      <ButtonGroup>
        {getPageNumbers(page, totalPages).map((p, i) =>
          p === '...' ? (
            <Button key={`ellipsis-${i}`} variant="outline" disabled>
              ...
            </Button>
          ) : (
            <Link key={p} to="/games" search={{ page: p }} hash={CATALOG_HASH}>
              <Button variant={p === page ? 'default' : 'outline'}>{p}</Button>
            </Link>
          ),
        )}
      </ButtonGroup>
      <Link
        to="/games"
        search={{ page: page + 1 }}
        hash={CATALOG_HASH}
        disabled={!hasNext}
      >
        <Button variant="outline" disabled={!hasNext}>
          Next
          <ArrowRight />
        </Button>
      </Link>
    </div>
  )
}
