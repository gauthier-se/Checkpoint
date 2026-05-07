import type { Priority } from '@/types/collection'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

interface PriorityBadgeProps {
  priority: Priority | null
  className?: string
}

const PRIORITY_LABELS: Record<Priority, string> = {
  LOW: 'Low',
  MEDIUM: 'Med',
  HIGH: 'High',
}

const PRIORITY_COLORS: Record<Priority, string> = {
  LOW: 'bg-slate-500/15 text-slate-400 border-slate-500/20',
  MEDIUM: 'bg-amber-500/15 text-amber-400 border-amber-500/20',
  HIGH: 'bg-red-500/15 text-red-400 border-red-500/20',
}

export function PriorityBadge({ priority, className }: PriorityBadgeProps) {
  if (priority === null) return null

  return (
    <Badge
      variant="outline"
      className={cn('border', PRIORITY_COLORS[priority], className)}
    >
      {PRIORITY_LABELS[priority]}
    </Badge>
  )
}
