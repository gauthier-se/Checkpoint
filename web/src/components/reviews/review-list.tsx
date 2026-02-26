import { useQuery } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { useState } from 'react'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { gameReviewsQueryOptions } from '@/queries/review'

interface ReviewListProps {
  gameId: string
}

export function ReviewList({ gameId }: ReviewListProps) {
  const [page, setPage] = useState(0)
  const size = 10

  const { data, isLoading, isError } = useQuery(
    gameReviewsQueryOptions(gameId, page, size),
  )

  if (isLoading) {
    return (
      <div className="py-8 text-center text-muted-foreground">
        Loading reviews...
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div className="py-8 text-center text-red-500">
        Error loading reviews.
      </div>
    )
  }

  const { content: reviews, metadata } = data

  if (reviews.length === 0) {
    return (
      <div className="py-8 text-center text-muted-foreground">
        No reviews yet. Be the first to leave one!
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <h2 className="text-xl font-semibold">User Reviews</h2>

      <div className="grid grid-cols-1 gap-4">
        {reviews.map((review) => (
          <Card key={review.id} className="py-4">
            <CardHeader className="py-0 pb-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Avatar className="h-10 w-10">
                    <AvatarImage
                      src={review.user.picture ?? undefined}
                      alt={review.user.pseudo}
                    />
                    <AvatarFallback>
                      {review.user.pseudo.substring(0, 2).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <CardTitle className="text-base">
                      {review.user.pseudo}
                    </CardTitle>
                    <CardDescription>
                      {new Date(review.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </CardDescription>
                  </div>
                </div>
                {review.score != null && (
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star
                        key={star}
                        className={`h-4 w-4 ${
                          star <= review.score!
                            ? 'fill-yellow-400 text-yellow-400'
                            : 'text-muted-foreground'
                        }`}
                      />
                    ))}
                  </div>
                )}
              </div>
            </CardHeader>
            <CardContent className="py-0">
              <p className="text-sm leading-relaxed whitespace-pre-line mt-2">
                {review.content}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Pagination Controls */}
      {metadata.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-4">
          <Button
            variant="outline"
            disabled={!metadata.hasPrevious}
            onClick={() => setPage((old) => Math.max(old - 1, 0))}
          >
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {metadata.page + 1} of {metadata.totalPages}
          </span>
          <Button
            variant="outline"
            disabled={!metadata.hasNext}
            onClick={() => setPage((old) => old + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  )
}
