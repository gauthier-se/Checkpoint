import { Link } from '@tanstack/react-router'
import { ArrowLeft, ArrowRight } from 'lucide-react'
import type { CollectionTab } from '@/types/collection'
import { Button } from '@/components/ui/button'
import { ButtonGroup } from '@/components/ui/button-group'
import { getPageNumbers } from '@/lib/pagination'

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
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-center gap-2 pt-6 pb-4">
      <Link to="." search={{ tab, page: page - 1 }} disabled={!hasPrevious}>
        <Button variant="outline" size="sm" disabled={!hasPrevious}>
          <ArrowLeft className="size-4" />
          Previous
        </Button>
      </Link>
      <ButtonGroup>
        {getPageNumbers(page, totalPages).map((p, i) =>
          p === '...' ? (
            <Button key={`ellipsis-${i}`} variant="outline" size="sm" disabled>
              …
            </Button>
          ) : (
            <Link key={p} to="." search={{ tab, page: p }}>
              <Button variant={p === page ? 'default' : 'outline'} size="sm">
                {p}
              </Button>
            </Link>
          ),
        )}
      </ButtonGroup>
      <Link to="." search={{ tab, page: page + 1 }} disabled={!hasNext}>
        <Button variant="outline" size="sm" disabled={!hasNext}>
          Next
          <ArrowRight className="size-4" />
        </Button>
      </Link>
    </div>
  )
}
