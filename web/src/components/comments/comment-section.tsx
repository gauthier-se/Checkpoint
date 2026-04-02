import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { MessageSquare, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'

import type { Comment, CommentsResponse } from '@/types/comment'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Textarea } from '@/components/ui/textarea'
import { useAuth } from '@/hooks/use-auth'
import {
  deleteComment,
  listCommentsQueryOptions,
  postListComment,
  postReviewComment,
  reviewCommentsQueryOptions,
  updateComment,
} from '@/queries/comment'

interface CommentSectionProps {
  targetType: 'review' | 'list'
  targetId: string
}

export function CommentSection({ targetType, targetId }: CommentSectionProps) {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [newComment, setNewComment] = useState('')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editContent, setEditContent] = useState('')
  const size = 10

  const queryOptions =
    targetType === 'review'
      ? reviewCommentsQueryOptions(targetId, page, size)
      : listCommentsQueryOptions(targetId, page, size)

  const { data, isLoading } = useQuery(queryOptions)

  const invalidateComments = () => {
    void queryClient.invalidateQueries({
      queryKey:
        targetType === 'review'
          ? ['reviews', targetId, 'comments']
          : ['lists', targetId, 'comments'],
    })
  }

  const postMutation = useMutation({
    mutationFn: (content: string) =>
      targetType === 'review'
        ? postReviewComment(targetId, content)
        : postListComment(targetId, content),
    onSuccess: () => {
      setNewComment('')
      invalidateComments()
    },
    onError: () => {
      toast.error('Failed to post comment')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ commentId, content }: { commentId: string; content: string }) =>
      updateComment(commentId, content),
    onSuccess: () => {
      setEditingId(null)
      setEditContent('')
      invalidateComments()
    },
    onError: () => {
      toast.error('Failed to update comment')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (commentId: string) => deleteComment(commentId),
    onMutate: async (commentId) => {
      const queryKey = queryOptions.queryKey
      await queryClient.cancelQueries({ queryKey })
      const previous = queryClient.getQueryData<CommentsResponse>(queryKey)
      queryClient.setQueryData<CommentsResponse>(queryKey, (old) => {
        if (!old) return old
        return {
          ...old,
          content: old.content.filter((c) => c.id !== commentId),
          metadata: {
            ...old.metadata,
            totalElements: old.metadata.totalElements - 1,
          },
        }
      })
      return { previous }
    },
    onError: (_err, _commentId, context) => {
      toast.error('Failed to delete comment')
      if (context?.previous) {
        queryClient.setQueryData(queryOptions.queryKey, context.previous)
      }
    },
    onSettled: () => {
      invalidateComments()
    },
  })

  const startEdit = (comment: Comment) => {
    setEditingId(comment.id)
    setEditContent(comment.content)
  }

  const cancelEdit = () => {
    setEditingId(null)
    setEditContent('')
  }

  const handleSubmitNew = (e: React.FormEvent) => {
    e.preventDefault()
    if (!newComment.trim()) return
    postMutation.mutate(newComment.trim())
  }

  const handleSubmitEdit = (e: React.FormEvent, commentId: string) => {
    e.preventDefault()
    if (!editContent.trim()) return
    updateMutation.mutate({ commentId, content: editContent.trim() })
  }

  return (
    <div className="space-y-4">
      <h3 className="flex items-center gap-2 text-lg font-semibold">
        <MessageSquare className="size-5" />
        Comments
        {data && data.metadata.totalElements > 0 && (
          <span className="text-sm font-normal text-muted-foreground">
            ({data.metadata.totalElements})
          </span>
        )}
      </h3>

      {/* New comment form */}
      {user ? (
        <form onSubmit={handleSubmitNew} className="space-y-3">
          <div className="flex gap-3">
            <Avatar className="mt-1 size-8 shrink-0">
              <AvatarImage src="/images/default-user.jpg" alt={user.username} />
              <AvatarFallback className="text-xs">
                {user.username.substring(0, 2).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 space-y-2">
              <Textarea
                placeholder="Write a comment..."
                className="min-h-[80px] resize-y"
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
              />
              <div className="flex justify-end">
                <Button
                  type="submit"
                  size="sm"
                  disabled={!newComment.trim() || postMutation.isPending}
                >
                  {postMutation.isPending ? 'Posting...' : 'Post Comment'}
                </Button>
              </div>
            </div>
          </div>
        </form>
      ) : (
        <p className="text-sm text-muted-foreground">
          Log in to leave a comment.
        </p>
      )}

      {/* Comments list */}
      {isLoading ? (
        <p className="text-sm text-muted-foreground">Loading comments...</p>
      ) : data && data.content.length > 0 ? (
        <div className="space-y-4">
          {data.content.map((comment) => (
            <div key={comment.id} className="flex gap-3">
              <Avatar className="mt-1 size-8 shrink-0">
                <AvatarImage
                  src={comment.user.picture ?? undefined}
                  alt={comment.user.pseudo}
                />
                <AvatarFallback className="text-xs">
                  {comment.user.pseudo.substring(0, 2).toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">
                    {comment.user.pseudo}
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {new Date(comment.createdAt).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'short',
                      day: 'numeric',
                    })}
                  </span>
                  {comment.createdAt !== comment.updatedAt && (
                    <span className="text-xs text-muted-foreground">(edited)</span>
                  )}
                  {user && user.id === comment.user.id && (
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="ml-auto h-7 w-7"
                        >
                          <MoreHorizontal className="size-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => startEdit(comment)}>
                          <Pencil className="mr-2 size-4" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="text-destructive"
                          onClick={() => deleteMutation.mutate(comment.id)}
                        >
                          <Trash2 className="mr-2 size-4" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  )}
                </div>
                {editingId === comment.id ? (
                  <form
                    onSubmit={(e) => handleSubmitEdit(e, comment.id)}
                    className="mt-2 space-y-2"
                  >
                    <Textarea
                      className="min-h-[60px] resize-y"
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                    />
                    <div className="flex gap-2">
                      <Button
                        type="submit"
                        size="sm"
                        disabled={
                          !editContent.trim() || updateMutation.isPending
                        }
                      >
                        {updateMutation.isPending ? 'Saving...' : 'Save'}
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={cancelEdit}
                      >
                        Cancel
                      </Button>
                    </div>
                  </form>
                ) : (
                  <p className="mt-1 text-sm leading-relaxed whitespace-pre-line">
                    {comment.content}
                  </p>
                )}
              </div>
            </div>
          ))}

          {/* Pagination */}
          {data.metadata.totalPages > 1 && (
            <div className="flex items-center justify-center gap-4 pt-2">
              <Button
                variant="outline"
                size="sm"
                disabled={!data.metadata.hasPrevious}
                onClick={() => setPage((old) => Math.max(old - 1, 0))}
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {data.metadata.page + 1} of {data.metadata.totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={!data.metadata.hasNext}
                onClick={() => setPage((old) => old + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </div>
      ) : (
        <p className="text-sm text-muted-foreground">
          No comments yet. Be the first to comment!
        </p>
      )}
    </div>
  )
}
