import { PlayLogForm } from './play-log-form'
import type { GameDetail } from '@/types/game'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

interface PlayLogDialogProps {
  game: GameDetail
  open: boolean
  onOpenChange: (open: boolean) => void
  onSuccess?: () => void
}

export function PlayLogDialog({
  game,
  open,
  onOpenChange,
  onSuccess,
}: PlayLogDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Log Play Session</DialogTitle>
          <DialogDescription>
            Record your playtime, dates, and thoughts for {game.title}.
          </DialogDescription>
        </DialogHeader>
        <PlayLogForm
          game={game}
          onCancel={() => onOpenChange(false)}
          onSuccess={() => {
            onSuccess?.()
            onOpenChange(false)
          }}
        />
      </DialogContent>
    </Dialog>
  )
}
