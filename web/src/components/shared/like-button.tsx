import { Heart } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'

interface LikeButtonProps {
  liked: boolean
  likesCount: number
  onToggle: () => void
  disabled: boolean
  isPending: boolean
}

export function LikeButton({
  liked,
  likesCount,
  onToggle,
  disabled,
  isPending,
}: LikeButtonProps) {
  const button = (
    <Button
      variant="ghost"
      size="sm"
      className="gap-1"
      disabled={disabled || isPending}
      onClick={onToggle}
    >
      <Heart className={`size-4 ${liked ? 'fill-current text-red-500' : ''}`} />
      {likesCount}
    </Button>
  )

  if (disabled && !isPending) {
    return (
      <Tooltip>
        <TooltipTrigger asChild>
          <span className="inline-block">{button}</span>
        </TooltipTrigger>
        <TooltipContent>
          <p>Log in to like this.</p>
        </TooltipContent>
      </Tooltip>
    )
  }

  return button
}
