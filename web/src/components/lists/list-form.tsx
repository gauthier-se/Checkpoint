import { useState } from 'react'
import { useForm } from '@tanstack/react-form'
import { useNavigate } from '@tanstack/react-router'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { z } from 'zod'
import {
  ChevronDown,
  ChevronUp,
  Gamepad2,
  Loader2,
  Trash2,
  X,
} from 'lucide-react'
import type { Game } from '@/types/game'
import type { GameListDetail, GameListEntry } from '@/types/list'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import {
  addGameToList,
  createList,
  deleteList,
  removeGameFromList,
  reorderListGames,
  updateList,
} from '@/queries/lists'
import { ListGameSearch } from '@/components/lists/list-game-search'

interface ListFormProps {
  mode: 'create' | 'edit'
  initialData?: GameListDetail
}

const listFormSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(100, 'Title must be 100 characters or fewer'),
  description: z
    .string()
    .max(500, 'Description must be 500 characters or fewer')
    .optional()
    .or(z.literal('')),
  isPrivate: z.boolean(),
})

export function ListForm({ mode, initialData }: ListFormProps) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [entries, setEntries] = useState<Array<GameListEntry>>(
    initialData?.entries ?? [],
  )
  const [isDeleting, setIsDeleting] = useState(false)
  const [gameActionLoading, setGameActionLoading] = useState<string | null>(
    null,
  )

  const form = useForm({
    defaultValues: {
      title: initialData?.title ?? '',
      description: initialData?.description ?? '',
      isPrivate: initialData?.isPrivate ?? false,
    },
    validators: {
      // @ts-expect-error Form library schema types are slightly off
      onChange: listFormSchema,
    },
    onSubmit: async ({ value }) => {
      try {
        if (mode === 'create') {
          const list = await createList({
            title: value.title,
            description: value.description || undefined,
            isPrivate: value.isPrivate,
          })

          for (const entry of entries) {
            await addGameToList(list.id, entry.videoGameId)
          }

          await queryClient.invalidateQueries({ queryKey: ['lists'] })
          toast.success('List created successfully!')
          await navigate({
            to: '/lists/$listId',
            params: { listId: list.id },
          })
        } else if (initialData) {
          await updateList(initialData.id, {
            title: value.title,
            description: value.description || undefined,
            isPrivate: value.isPrivate,
          })

          await queryClient.invalidateQueries({ queryKey: ['lists'] })
          toast.success('List updated successfully!')
          await navigate({
            to: '/lists/$listId',
            params: { listId: initialData.id },
          })
        }
      } catch {
        toast.error(
          mode === 'create'
            ? 'Failed to create list.'
            : 'Failed to update list.',
        )
      }
    },
  })

  async function handleAddGame(game: Game) {
    if (mode === 'edit' && initialData) {
      setGameActionLoading(game.id)
      try {
        const updated = await addGameToList(initialData.id, game.id)
        setEntries(updated.entries)
        await queryClient.invalidateQueries({
          queryKey: ['lists', initialData.id],
        })
      } catch {
        toast.error('Failed to add game.')
      } finally {
        setGameActionLoading(null)
      }
    } else {
      const newEntry: GameListEntry = {
        videoGameId: game.id,
        title: game.title,
        coverUrl: game.coverUrl,
        releaseDate: game.releaseDate,
        position: entries.length,
        addedAt: new Date().toISOString(),
      }
      setEntries((prev) => [...prev, newEntry])
    }
  }

  async function handleRemoveGame(videoGameId: string) {
    if (mode === 'edit' && initialData) {
      setGameActionLoading(videoGameId)
      try {
        await removeGameFromList(initialData.id, videoGameId)
        setEntries((prev) =>
          prev
            .filter((e) => e.videoGameId !== videoGameId)
            .map((e, i) => ({ ...e, position: i })),
        )
        await queryClient.invalidateQueries({
          queryKey: ['lists', initialData.id],
        })
      } catch {
        toast.error('Failed to remove game.')
      } finally {
        setGameActionLoading(null)
      }
    } else {
      setEntries((prev) =>
        prev
          .filter((e) => e.videoGameId !== videoGameId)
          .map((e, i) => ({ ...e, position: i })),
      )
    }
  }

  async function handleMoveGame(index: number, direction: 'up' | 'down') {
    const newIndex = direction === 'up' ? index - 1 : index + 1
    if (newIndex < 0 || newIndex >= entries.length) return

    const reordered = [...entries]
    const [moved] = reordered.splice(index, 1)
    reordered.splice(newIndex, 0, moved)
    const updated = reordered.map((e, i) => ({ ...e, position: i }))
    setEntries(updated)

    if (mode === 'edit' && initialData) {
      try {
        await reorderListGames(
          initialData.id,
          updated.map((e) => e.videoGameId),
        )
        await queryClient.invalidateQueries({
          queryKey: ['lists', initialData.id],
        })
      } catch {
        toast.error('Failed to reorder games.')
        setEntries(entries)
      }
    }
  }

  async function handleDeleteList() {
    if (!initialData) return
    setIsDeleting(true)
    try {
      await deleteList(initialData.id)
      await queryClient.invalidateQueries({ queryKey: ['lists'] })
      toast.success('List deleted successfully!')
      await navigate({ to: '/lists', search: { page: 1 } })
    } catch {
      toast.error('Failed to delete list.')
      setIsDeleting(false)
    }
  }

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        e.stopPropagation()
        void form.handleSubmit()
      }}
      className="space-y-8"
    >
      <div className="space-y-4">
        <form.Field
          name="title"
          children={(field) => (
            <div className="space-y-2">
              <Label htmlFor={field.name}>Title *</Label>
              <Input
                id={field.name}
                placeholder="My game list"
                value={field.state.value}
                onChange={(e) => field.handleChange(e.target.value)}
              />
              {field.state.meta.errors.length > 0 ? (
                <p className="text-sm text-destructive">
                  {field.state.meta.errors
                    .map((e) =>
                      typeof e === 'string' ? e : (e as any).message,
                    )
                    .join(', ')}
                </p>
              ) : null}
            </div>
          )}
        />

        <form.Field
          name="description"
          children={(field) => (
            <div className="space-y-2">
              <Label htmlFor={field.name}>Description</Label>
              <Textarea
                id={field.name}
                placeholder="Describe your list..."
                className="resize-y min-h-[80px]"
                value={field.state.value}
                onChange={(e) => field.handleChange(e.target.value)}
              />
              {field.state.meta.errors.length > 0 ? (
                <p className="text-sm text-destructive">
                  {field.state.meta.errors
                    .map((e) =>
                      typeof e === 'string' ? e : (e as any).message,
                    )
                    .join(', ')}
                </p>
              ) : null}
            </div>
          )}
        />

        <form.Field
          name="isPrivate"
          children={(field) => (
            <div className="flex items-center gap-2 pt-2">
              <Switch
                id={field.name}
                checked={field.state.value}
                onCheckedChange={field.handleChange}
              />
              <Label htmlFor={field.name} className="cursor-pointer">
                Private list
              </Label>
            </div>
          )}
        />
      </div>

      <div className="space-y-4">
        <h3 className="text-md font-medium">Games</h3>

        <ListGameSearch
          onSelect={handleAddGame}
          excludeIds={entries.map((e) => e.videoGameId)}
        />

        {entries.length > 0 ? (
          <ul className="space-y-2">
            {entries.map((entry, index) => (
              <li
                key={entry.videoGameId}
                className="flex items-center gap-3 rounded-md border p-3"
              >
                <span className="text-sm font-medium text-muted-foreground w-6 text-center shrink-0">
                  {entry.position + 1}
                </span>
                {entry.coverUrl ? (
                  <img
                    src={entry.coverUrl}
                    alt=""
                    className="h-12 w-9 rounded-sm object-cover shrink-0"
                  />
                ) : (
                  <div className="flex h-12 w-9 items-center justify-center rounded-sm bg-muted shrink-0">
                    <Gamepad2 className="size-4 text-muted-foreground" />
                  </div>
                )}
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-sm truncate">{entry.title}</p>
                  {entry.releaseDate && (
                    <p className="text-xs text-muted-foreground">
                      {new Date(entry.releaseDate).getFullYear()}
                    </p>
                  )}
                </div>
                {gameActionLoading === entry.videoGameId ? (
                  <Loader2 className="size-4 animate-spin text-muted-foreground shrink-0" />
                ) : (
                  <div className="flex items-center gap-1 shrink-0">
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="size-7"
                      disabled={index === 0}
                      onClick={() => handleMoveGame(index, 'up')}
                    >
                      <ChevronUp className="size-4" />
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="size-7"
                      disabled={index === entries.length - 1}
                      onClick={() => handleMoveGame(index, 'down')}
                    >
                      <ChevronDown className="size-4" />
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="size-7 text-destructive hover:text-destructive"
                      onClick={() => handleRemoveGame(entry.videoGameId)}
                    >
                      <X className="size-4" />
                    </Button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <div className="flex flex-col items-center gap-2 rounded-md border border-dashed py-8 text-center">
            <Gamepad2 className="size-8 text-muted-foreground" />
            <p className="text-sm text-muted-foreground">
              No games added yet. Search above to add games.
            </p>
          </div>
        )}
      </div>

      <div className="flex items-center justify-between pt-4">
        {mode === 'edit' && initialData ? (
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button type="button" variant="destructive" disabled={isDeleting}>
                {isDeleting ? (
                  <Loader2 className="size-4 animate-spin" />
                ) : (
                  <Trash2 className="size-4" />
                )}
                Delete list
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Delete this list?</AlertDialogTitle>
                <AlertDialogDescription>
                  This action cannot be undone. The list &quot;
                  {initialData.title}
                  &quot; and all its entries will be permanently deleted.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Cancel</AlertDialogCancel>
                <AlertDialogAction
                  onClick={handleDeleteList}
                  className="bg-destructive text-white hover:bg-destructive/90"
                >
                  Delete
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        ) : (
          <div />
        )}

        <form.Subscribe
          selector={(s) => [s.canSubmit, s.isSubmitting]}
          children={([canSubmit, isSubmitting]) => (
            <Button type="submit" disabled={!canSubmit || isSubmitting}>
              {isSubmitting
                ? 'Saving...'
                : mode === 'create'
                  ? 'Create List'
                  : 'Save Changes'}
            </Button>
          )}
        />
      </div>
    </form>
  )
}
