import { FeedItem } from './feed-item'
import type { FeedItem as FeedItemType } from '@/types/feed'
import { Skeleton } from '@/components/ui/skeleton'

interface FeedListProps {
  items: Array<FeedItemType>
}

export function FeedList({ items }: FeedListProps) {
  return (
    <div className="divide-y py-2">
      {items.map((item) => (
        <FeedItem key={item.id} item={item} />
      ))}
    </div>
  )
}

function FeedItemSkeleton() {
  return (
    <div className="flex items-start gap-3 py-3">
      <Skeleton className="size-9 shrink-0 rounded-full" />
      <div className="min-w-0 flex-1 space-y-2 py-1">
        <Skeleton className="h-4 w-2/3" />
        <Skeleton className="h-3 w-24" />
      </div>
      <Skeleton className="h-14 w-10 shrink-0 rounded" />
    </div>
  )
}

export function FeedListSkeleton({ count = 5 }: { count?: number }) {
  return (
    <div className="divide-y py-2">
      {Array.from({ length: count }).map((_, i) => (
        <FeedItemSkeleton key={i} />
      ))}
    </div>
  )
}
