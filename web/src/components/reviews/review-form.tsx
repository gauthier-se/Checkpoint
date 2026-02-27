import { useForm } from '@tanstack/react-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { useState } from 'react'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { submitReview } from '@/queries/review'
import type { Review } from '@/types/review'

interface ReviewFormProps {
  gameId: string
  existingReview: Review | null
  onSuccess?: () => void
}

export function ReviewForm({
  gameId,
  existingReview,
  onSuccess,
}: ReviewFormProps) {
  const queryClient = useQueryClient()
  const [isEditing, setIsEditing] = useState(!existingReview)

  const reviewSchema = z.object({
    score: z.number().min(1, 'Select a rating').max(5, 'Score maximum is 5'),
    content: z.string().optional().default(''),
    haveSpoilers: z.boolean().default(false),
  })

  const mutation = useMutation({
    mutationFn: (values: {
      score: number
      content: string
      haveSpoilers: boolean
    }) => submitReview(gameId, values),
    onSuccess: (newReview) => {
      // Invalidate both the game reviews list and the user's specific review
      queryClient.invalidateQueries({ queryKey: ['games', gameId, 'reviews'] })
      // Update the explicit "me" query with the new data
      queryClient.setQueryData(['games', gameId, 'reviews', 'me'], newReview)
      setIsEditing(false)
      onSuccess?.()
    },
  })

  const form = useForm({
    defaultValues: {
      score: existingReview?.score ?? 0,
      content: existingReview?.content ?? '',
      haveSpoilers: existingReview?.haveSpoilers ?? false,
    },
    validators: {
      // @ts-expect-error Zod schema compatibility with form validator definition is slightly off
      onChange: reviewSchema,
    },
    onSubmit: ({ value }) => {
      mutation.mutate(value)
    },
  })

  if (existingReview && !isEditing) {
    return (
      <div className="bg-muted min-h-[140px] rounded-lg p-6 flex flex-col items-center justify-center gap-4 text-center">
        <div className="flex flex-col items-center gap-2">
          <p className="font-medium">You have already reviewed this game.</p>
          <div className="flex items-center gap-1">
            {[1, 2, 3, 4, 5].map((star) => (
              <Star
                key={star}
                className={`h-5 w-5 ${
                  star <= existingReview.score!
                    ? 'fill-yellow-400 text-yellow-500'
                    : 'text-muted-foreground opacity-30'
                }`}
              />
            ))}
          </div>
        </div>
        <Button variant="outline" onClick={() => setIsEditing(true)}>
          Edit your review
        </Button>
      </div>
    )
  }

  return (
    <div className="bg-muted p-6 rounded-lg border">
      <h3 className="text-lg font-semibold mb-4">
        {existingReview ? 'Edit Your Review' : 'Write a Review'}
      </h3>
      <form
        onSubmit={(e) => {
          e.preventDefault()
          e.stopPropagation()
          form.handleSubmit()
        }}
        className="flex flex-col gap-6"
      >
        <form.Field
          name="score"
          children={(field) => (
            <div className="flex flex-col gap-2">
              <Label>
                Rating <span className="text-red-500">*</span>
              </Label>
              <div
                className="flex gap-1"
                onMouseLeave={() => field.handleChange(field.state.value)}
              >
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    className="p-1 -ml-1 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm"
                    onClick={() => field.handleChange(star)}
                  >
                    <Star
                      className={`h-8 w-8 transition-colors ${
                        star <= field.state.value
                          ? 'fill-yellow-400 text-yellow-500 hover:fill-yellow-300'
                          : 'text-muted-foreground/30 hover:text-muted-foreground/50'
                      }`}
                    />
                  </button>
                ))}
              </div>
              {field.state.meta.errors.length > 0 ? (
                <em className="text-sm text-destructive" role="alert">
                  {field.state.meta.errors.join(', ')}
                </em>
              ) : null}
            </div>
          )}
        />

        <form.Field
          name="content"
          children={(field) => (
            <div className="flex flex-col gap-2">
              <Label htmlFor="content">Review (optional)</Label>
              <Textarea
                id="content"
                placeholder="What did you think about the game?"
                className="resize-y min-h-[100px]"
                value={field.state.value}
                onChange={(e) => field.handleChange(e.target.value)}
              />
            </div>
          )}
        />

        <form.Field
          name="haveSpoilers"
          children={(field) => (
            <div className="flex items-center gap-2">
              <Switch
                id="haveSpoilers"
                checked={field.state.value}
                onCheckedChange={field.handleChange}
              />
              <Label
                htmlFor="haveSpoilers"
                className="font-normal cursor-pointer"
              >
                Contains spoilers
              </Label>
            </div>
          )}
        />

        <div className="flex justify-end gap-2 mt-2">
          {existingReview && (
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                form.reset()
                setIsEditing(false)
              }}
              disabled={mutation.isPending}
            >
              Cancel
            </Button>
          )}
          <form.Subscribe
            selector={(state) => [state.canSubmit, state.isSubmitting]}
            children={([canSubmit]) => (
              <Button type="submit" disabled={!canSubmit || mutation.isPending}>
                {mutation.isPending ? 'Submitting...' : 'Submit Review'}
              </Button>
            )}
          />
        </div>
        {mutation.isError && (
          <p className="text-sm text-destructive mt-2 text-right">
            Failed to submit review.
          </p>
        )}
      </form>
    </div>
  )
}
