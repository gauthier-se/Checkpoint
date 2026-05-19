import { PlayLogForm } from './play-log-form'
import type { GameDetail } from '@/types/game'
import type { PlayLogDetail } from '@/types/play-log'
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
  initialPlayLog?: PlayLogDetail
}

export function PlayLogDialog({
  game,
  open,
  onOpenChange,
  onSuccess,
  initialPlayLog,
}: PlayLogDialogProps) {
  const isEdit = !!initialPlayLog
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {isEdit ? 'Edit Play Session' : 'Log Play Session'}
          </DialogTitle>
          <DialogDescription>
            {isEdit
              ? `Update your play session for ${game.title}.`
              : `Record your playtime, dates, and thoughts for ${game.title}.`}
          </DialogDescription>
        </DialogHeader>
        {isEdit ? (
          <PlayLogForm
            mode="edit"
            game={game}
            initialPlayLog={initialPlayLog}
            onCancel={() => onOpenChange(false)}
            onSuccess={() => {
              onSuccess?.()
              onOpenChange(false)
            }}
          />
        ) : (
          <PlayLogForm
            game={game}
            onCancel={() => onOpenChange(false)}
            onSuccess={() => {
              onSuccess?.()
              onOpenChange(false)
            }}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
