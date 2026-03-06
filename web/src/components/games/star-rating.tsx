import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { toast } from 'sonner'
import type { GameDetail } from '@/types/game'
import type { GameInteractionStatusDto } from '@/types/interaction'
import { useAuth } from '@/hooks/use-auth'
import {
  gameInteractionStatusQueryOptions,
  rateGame,
  removeRating,
} from '@/queries/games'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'

interface StarRatingProps {
  game: GameDetail
  currentRating: number | null
}

export function StarRating({ game, currentRating }: StarRatingProps) {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [hoveredScore, setHoveredScore] = useState<number | null>(null)

  const ratingMutation = useMutation({
    mutationFn: async (score: number) => {
      if (score === currentRating) {
        // Toggle off if clicking the same score
        await removeRating(game.id)
        return null
      }
      const res = await rateGame(game.id, score)
      return res.score
    },
    onMutate: async (newScore) => {
      await queryClient.cancelQueries(
        gameInteractionStatusQueryOptions(game.id),
      )
      const previous = queryClient.getQueryData<GameInteractionStatusDto>(
        gameInteractionStatusQueryOptions(game.id).queryKey,
      )
      queryClient.setQueryData<GameInteractionStatusDto>(
        gameInteractionStatusQueryOptions(game.id).queryKey,
        (old) => {
          if (!old) return old
          return {
            ...old,
            userRating: newScore === currentRating ? null : newScore,
          }
        },
      )
      return { previous }
    },
    onError: (_err, _variables, context) => {
      toast.error('Failed to update rating')
      if (context?.previous) {
        queryClient.setQueryData(
          gameInteractionStatusQueryOptions(game.id).queryKey,
          context.previous,
        )
      }
    },
    onSettled: () => {
      void queryClient.invalidateQueries(
        gameInteractionStatusQueryOptions(game.id),
      )
      // Also invalidate the game detail query so the average rating updates
      void queryClient.invalidateQueries({ queryKey: ['games', game.id] })
    },
    onSuccess: (newScore) => {
      if (newScore === null) {
        toast.success('Rating removed')
      } else {
        toast.success(`Rated ${newScore}/5`)
      }
    },
  })

  const disabled = !user || ratingMutation.isPending
  const displayScore = hoveredScore ?? currentRating ?? 0

  const stars = (
    <div
      className={`flex gap-0.5 ${disabled ? 'opacity-50 pointer-events-none' : ''}`}
      onMouseLeave={() => setHoveredScore(null)}
    >
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          disabled={disabled}
          className="p-1 -ml-1 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm transition-transform active:scale-90"
          onMouseEnter={() => setHoveredScore(star)}
          onClick={() => ratingMutation.mutate(star)}
        >
          <Star
            className={`h-6 w-6 transition-colors ${
              star <= displayScore
                ? 'fill-yellow-400 text-yellow-500 hover:fill-yellow-300'
                : 'text-muted-foreground/30 hover:text-muted-foreground/50'
            }`}
          />
        </button>
      ))}
    </div>
  )

  if (!user) {
    return (
      <Tooltip>
        <TooltipTrigger asChild>
          <div className="inline-block cursor-not-allowed">{stars}</div>
        </TooltipTrigger>
        <TooltipContent>
          <p>Log in to rate</p>
        </TooltipContent>
      </Tooltip>
    )
  }

  return stars
}
