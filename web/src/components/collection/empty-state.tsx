import { Link } from '@tanstack/react-router'
import { Button } from '@/components/ui/button'

interface EmptyStateProps {
  icon: React.ReactNode
  title: string
  description: string
  actionLabel?: string
  actionTo?: string
}

export function EmptyState({
  icon,
  title,
  description,
  actionLabel,
  actionTo,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="mb-4 text-muted-foreground/40">{icon}</div>
      <h3 className="text-lg font-semibold">{title}</h3>
      <p className="mt-1 max-w-sm text-sm text-muted-foreground">
        {description}
      </p>
      {actionLabel && actionTo && (
        <Link to={actionTo} className="mt-6">
          <Button>{actionLabel}</Button>
        </Link>
      )}
    </div>
  )
}
