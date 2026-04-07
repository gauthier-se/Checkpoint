import type { FeedItem as FeedItemType } from '@/types/feed'
import { FeedItem } from './feed-item'

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
