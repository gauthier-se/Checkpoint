import { useForm } from '@tanstack/react-form'
import { toast } from 'sonner'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { reportReview } from '@/queries/review'

interface ReportReviewDialogProps {
  reviewId: string
  open: boolean
  onOpenChange: (open: boolean) => void
}

const reportSchema = z.object({
  content: z.string().min(1, 'Reason is required'),
})

export function ReportReviewDialog({
  reviewId,
  open,
  onOpenChange,
}: ReportReviewDialogProps) {
  const form = useForm({
    defaultValues: {
      content: '',
    },
    validators: {
      // @ts-expect-error Form library schema types are slightly off
      onSubmit: reportSchema,
    },
    onSubmit: async ({ value }) => {
      try {
        await reportReview(reviewId, { content: value.content })
        toast.success('Review reported successfully')
        onOpenChange(false)
        form.reset()
      } catch (err) {
        if (err instanceof Error) {
          toast.error(err.message)
        } else {
          toast.error('Failed to report review')
        }
      }
    },
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[450px]">
        <DialogHeader>
          <DialogTitle>Report Review</DialogTitle>
          <DialogDescription>
            Tell us why you think this review should be reviewed by moderators.
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={(e) => {
            e.preventDefault()
            e.stopPropagation()
            void form.handleSubmit()
          }}
          className="space-y-4 py-2"
        >
          <form.Field
            name="content"
            children={(field) => (
              <div className="space-y-2">
                <Label htmlFor={field.name}>Reason *</Label>
                <Textarea
                  id={field.name}
                  placeholder="Describe why this review is inappropriate..."
                  className="resize-y min-h-[100px]"
                  value={field.state.value}
                  onChange={(e) => field.handleChange(e.target.value)}
                />
                {field.state.meta.errors.length > 0 && (
                  <p className="text-sm text-destructive">
                    {field.state.meta.errors
                      .map((e) =>
                        typeof e === 'string' ? e : (e as any).message,
                      )
                      .join(', ')}
                  </p>
                )}
              </div>
            )}
          />

          <form.Subscribe
            selector={(s) => [s.canSubmit, s.isSubmitting]}
            children={([canSubmit, isSubmitting]) => (
              <div className="flex justify-end gap-2">
                <Button
                  variant="outline"
                  type="button"
                  onClick={() => onOpenChange(false)}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="destructive"
                  disabled={!canSubmit || isSubmitting}
                >
                  {isSubmitting ? 'Reporting...' : 'Report'}
                </Button>
              </div>
            )}
          />
        </form>
      </DialogContent>
    </Dialog>
  )
}
