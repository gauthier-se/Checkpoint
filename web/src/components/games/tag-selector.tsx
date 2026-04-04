import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Check, ChevronsUpDown, Plus, Tag, X } from 'lucide-react'
import type { Tag as TagType } from '@/types/tag'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { cn } from '@/lib/utils'
import { createTag, myTagsQueryOptions } from '@/queries/tags'

interface TagSelectorProps {
  selectedTagIds: Array<string>
  onChange: (tagIds: Array<string>) => void
}

export function TagSelector({ selectedTagIds, onChange }: TagSelectorProps) {
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const queryClient = useQueryClient()

  const { data: tags = [] } = useQuery(myTagsQueryOptions())

  const createMutation = useMutation({
    mutationFn: (name: string) => createTag({ name }),
    onSuccess: (newTag: TagType) => {
      void queryClient.invalidateQueries({ queryKey: ['tags', 'me'] })
      onChange([...selectedTagIds, newTag.id])
      setSearch('')
    },
  })

  const selectedTags = tags.filter((tag) => selectedTagIds.includes(tag.id))

  const exactMatch = tags.some(
    (tag) => tag.name.toLowerCase() === search.trim().toLowerCase(),
  )

  function toggleTag(tagId: string) {
    if (selectedTagIds.includes(tagId)) {
      onChange(selectedTagIds.filter((id) => id !== tagId))
    } else {
      onChange([...selectedTagIds, tagId])
    }
  }

  function removeTag(tagId: string) {
    onChange(selectedTagIds.filter((id) => id !== tagId))
  }

  return (
    <div className="space-y-2">
      {selectedTags.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {selectedTags.map((tag) => (
            <Badge key={tag.id} variant="secondary" className="gap-1 pr-1">
              <Tag className="size-3" />
              {tag.name}
              <button
                type="button"
                className="ml-0.5 rounded-full p-0.5 hover:bg-muted-foreground/20"
                onClick={() => removeTag(tag.id)}
              >
                <X className="size-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}

      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            role="combobox"
            aria-expanded={open}
            type="button"
            className="w-full justify-between text-muted-foreground font-normal"
          >
            Select tags...
            <ChevronsUpDown className="size-4 shrink-0 opacity-50" />
          </Button>
        </PopoverTrigger>
        <PopoverContent
          className="w-[--radix-popover-trigger-width] p-0"
          align="start"
        >
          <Command>
            <CommandInput
              placeholder="Search or create tag..."
              value={search}
              onValueChange={setSearch}
            />
            <CommandList>
              <CommandEmpty>
                {search.trim() ? 'No tags found.' : 'No tags yet.'}
              </CommandEmpty>
              <CommandGroup>
                {tags.map((tag) => (
                  <CommandItem
                    key={tag.id}
                    value={tag.name}
                    onSelect={() => toggleTag(tag.id)}
                  >
                    <Check
                      className={cn(
                        'size-4',
                        selectedTagIds.includes(tag.id)
                          ? 'opacity-100'
                          : 'opacity-0',
                      )}
                    />
                    <Tag className="size-3.5 text-muted-foreground" />
                    {tag.name}
                    <span className="ml-auto text-xs text-muted-foreground">
                      {tag.playLogsCount}
                    </span>
                  </CommandItem>
                ))}
              </CommandGroup>
              {search.trim() && !exactMatch && (
                <CommandGroup>
                  <CommandItem
                    value={`create-${search.trim()}`}
                    onSelect={() => createMutation.mutate(search.trim())}
                    disabled={createMutation.isPending}
                  >
                    <Plus className="size-4" />
                    Create &quot;{search.trim()}&quot;
                  </CommandItem>
                </CommandGroup>
              )}
            </CommandList>
          </Command>
        </PopoverContent>
      </Popover>
    </div>
  )
}
