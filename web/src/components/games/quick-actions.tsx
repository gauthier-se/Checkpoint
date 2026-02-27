import { useState } from 'react'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Bookmark,
  Check,
  ChevronDown,
  Gamepad2,
  Heart,
  Library,
  Trash2,
} from 'lucide-react'
import { toast } from 'sonner'
import type { GameDetail } from '@/types/game'
import type { GameInteractionStatusDto } from '@/types/interaction'
import type { GameStatus } from '@/types/library'
import { PlayLogDialog } from '@/components/games/play-log-dialog'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { useAuth } from '@/hooks/use-auth'
import {
  gameInteractionStatusQueryOptions,
  toggleBacklog,
  toggleWishlist,
  updateLibraryStatus,
} from '@/queries/games'

interface GameQuickActionsProps {
  game: GameDetail
}

export function GameQuickActions({ game }: GameQuickActionsProps) {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [playLogOpen, setPlayLogOpen] = useState(false)

  const { data: status } = useQuery({
    ...gameInteractionStatusQueryOptions(game.id),
    enabled: !!user,
  })

  // Mutations with optimistic updates
  const wishlistMutation = useMutation({
    mutationFn: () => toggleWishlist(game.id, status?.inWishlist ?? false),
    onMutate: async () => {
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
          return { ...old, inWishlist: !old.inWishlist }
        },
      )
      return { previous }
    },
    onError: (_err, _variables, context) => {
      toast.error('Failed to update wishlist')
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
    },
    onSuccess: (_, __, context) => {
      // If previous was false, it means we added it
      if (!context.previous?.inWishlist) {
        toast.success('Added to wishlist')
      } else {
        toast.success('Removed from wishlist')
      }
    },
  })

  const backlogMutation = useMutation({
    mutationFn: () => toggleBacklog(game.id, status?.inBacklog ?? false),
    onMutate: async () => {
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
          return { ...old, inBacklog: !old.inBacklog }
        },
      )
      return { previous }
    },
    onError: (_err, _variables, context) => {
      toast.error('Failed to update backlog')
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
    },
    onSuccess: (_, __, context) => {
      if (!context.previous?.inBacklog) {
        toast.success('Added to backlog')
      } else {
        toast.success('Removed from backlog')
      }
    },
  })

  const libraryMutation = useMutation({
    mutationFn: (newStatus: GameStatus | null) =>
      updateLibraryStatus(
        game.id,
        newStatus ? { videoGameId: game.id, status: newStatus } : null,
      ),
    onMutate: async (newStatus) => {
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
            inLibrary: newStatus !== null,
            libraryStatus: newStatus,
          }
        },
      )
      return { previous }
    },
    onError: (_err, _variables, context) => {
      toast.error('Failed to update library')
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
    },
    onSuccess: (_data, newStatus) => {
      if (newStatus === null) {
        toast.success('Removed from library')
      } else {
        toast.success(`Library status set to ${newStatus}`)
      }
    },
  })

  const handleLibraryChange = (newStatus: GameStatus) => {
    libraryMutation.mutate(newStatus)
  }

  const handleRemoveFromLibrary = () => {
    libraryMutation.mutate(null)
  }

  const renderButtons = () => {
    const disabled = !user
    const isWishlisted = status?.inWishlist
    const isBacklog = status?.inBacklog
    const libraryStatus = status?.libraryStatus

    const buttons = (
      <div className="flex flex-wrap items-center gap-2">
        {/* Wishlist Button */}
        <Button
          variant={isWishlisted ? 'default' : 'outline'}
          size="sm"
          className="gap-2"
          disabled={disabled || wishlistMutation.isPending}
          onClick={() => wishlistMutation.mutate()}
        >
          <Heart className={`w-4 h-4 ${isWishlisted ? 'fill-current' : ''}`} />
          Wishlist
        </Button>

        {/* Backlog Button */}
        <Button
          variant={isBacklog ? 'default' : 'outline'}
          size="sm"
          className="gap-2"
          disabled={disabled || backlogMutation.isPending}
          onClick={() => backlogMutation.mutate()}
        >
          <Bookmark className={`w-4 h-4 ${isBacklog ? 'fill-current' : ''}`} />
          Backlog
        </Button>

        {/* Library Dropdown */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant={libraryStatus ? 'default' : 'outline'}
              size="sm"
              className="gap-2 focus:ring-0"
              disabled={disabled || libraryMutation.isPending}
            >
              <Library className="w-4 h-4" />
              {libraryStatus ? `Lib: ${libraryStatus}` : 'Library'}
              <ChevronDown className="w-3 h-3 opacity-50" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start">
            <DropdownMenuItem onClick={() => handleLibraryChange('PLAYING')}>
              {libraryStatus === 'PLAYING' && (
                <Check className="w-4 h-4 mr-2" />
              )}
              <span className={libraryStatus === 'PLAYING' ? '' : 'ml-6'}>
                Playing
              </span>
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => handleLibraryChange('COMPLETED')}>
              {libraryStatus === 'COMPLETED' && (
                <Check className="w-4 h-4 mr-2" />
              )}
              <span className={libraryStatus === 'COMPLETED' ? '' : 'ml-6'}>
                Completed
              </span>
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => handleLibraryChange('DROPPED')}>
              {libraryStatus === 'DROPPED' && (
                <Check className="w-4 h-4 mr-2" />
              )}
              <span className={libraryStatus === 'DROPPED' ? '' : 'ml-6'}>
                Dropped
              </span>
            </DropdownMenuItem>
            {libraryStatus && (
              <>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  className="text-destructive focus:text-destructive"
                  onClick={handleRemoveFromLibrary}
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Remove from Library
                </DropdownMenuItem>
              </>
            )}
          </DropdownMenuContent>
        </DropdownMenu>

        {/* Play Log Button */}
        <Button
          variant="outline"
          size="sm"
          className="gap-2"
          disabled={disabled}
          onClick={() => setPlayLogOpen(true)}
        >
          <Gamepad2 className="w-4 h-4" />
          Log Play
        </Button>
      </div>
    )

    if (disabled) {
      return (
        <Tooltip>
          <TooltipTrigger asChild>
            <div className="inline-block cursor-not-allowed">
              {/* Note: we wrap in a div so the tooltip triggers even when buttons are disabled */}
              {buttons}
            </div>
          </TooltipTrigger>
          <TooltipContent>
            <p>Log in to manage your collection and play logs.</p>
          </TooltipContent>
        </Tooltip>
      )
    }

    return buttons
  }

  return (
    <>
      <div className="py-2">{renderButtons()}</div>

      <PlayLogDialog
        game={game}
        open={playLogOpen}
        onOpenChange={setPlayLogOpen}
        onSuccess={() => {
          void queryClient.invalidateQueries({
            queryKey: ['games', game.id],
          })
        }}
      />
    </>
  )
}
