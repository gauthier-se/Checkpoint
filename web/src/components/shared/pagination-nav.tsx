import { Link } from '@tanstack/react-router'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { LinkProps } from '@tanstack/react-router'
import { cn } from '@/lib/utils'
import { buttonVariants } from '@/components/ui/button'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
} from '@/components/ui/pagination'
import { getPageNumbers } from '@/lib/pagination'

interface PaginationNavProps {
  page: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  /** Builds the router link props (to / search / hash / params) for a target page. */
  linkProps: (page: number) => LinkProps
  /** Hide entirely when there is only a single page (used by tighter contexts). */
  hideWhenSinglePage?: boolean
  className?: string
}

const EDGE_CLASS = 'gap-1 px-2.5'
const DISABLED_EDGE_CLASS = cn(
  buttonVariants({ variant: 'ghost', size: 'default' }),
  EDGE_CLASS,
  'pointer-events-none opacity-50',
)

/**
 * Shared activity/listing pagination built on the shadcn `Pagination`
 * primitives. Each caller supplies `linkProps(page)` returning the TanStack
 * Router link props for navigating to a given page.
 */
export function PaginationNav({
  page,
  totalPages,
  hasNext,
  hasPrevious,
  linkProps,
  hideWhenSinglePage = false,
  className,
}: PaginationNavProps) {
  if (hideWhenSinglePage && totalPages <= 1) {
    return null
  }

  return (
    <Pagination className={className}>
      <PaginationContent>
        <PaginationItem>
          {hasPrevious ? (
            <PaginationLink asChild size="default" className={EDGE_CLASS}>
              <Link {...linkProps(page - 1)}>
                <ChevronLeft />
                <span className="hidden sm:block">Previous</span>
              </Link>
            </PaginationLink>
          ) : (
            <span aria-disabled className={DISABLED_EDGE_CLASS}>
              <ChevronLeft />
              <span className="hidden sm:block">Previous</span>
            </span>
          )}
        </PaginationItem>

        {getPageNumbers(page, totalPages).map((p, i) =>
          p === '...' ? (
            <PaginationItem key={`ellipsis-${i}`}>
              <PaginationEllipsis />
            </PaginationItem>
          ) : (
            <PaginationItem key={p}>
              <PaginationLink asChild isActive={p === page}>
                <Link {...linkProps(p)}>{p}</Link>
              </PaginationLink>
            </PaginationItem>
          ),
        )}

        <PaginationItem>
          {hasNext ? (
            <PaginationLink asChild size="default" className={EDGE_CLASS}>
              <Link {...linkProps(page + 1)}>
                <span className="hidden sm:block">Next</span>
                <ChevronRight />
              </Link>
            </PaginationLink>
          ) : (
            <span aria-disabled className={DISABLED_EDGE_CLASS}>
              <span className="hidden sm:block">Next</span>
              <ChevronRight />
            </span>
          )}
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}
