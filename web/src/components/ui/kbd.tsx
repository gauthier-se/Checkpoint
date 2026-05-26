import * as React from 'react'
import { Command } from 'lucide-react'
import { cn } from '@/lib/utils'

function KbdGroup({ className, ...props }: React.ComponentProps<'kbd'>) {
  return (
    <kbd
      data-slot="kbd-group"
      className={cn('inline-flex items-center gap-1', className)}
      {...props}
    />
  )
}

function Kbd({ className, ...props }: React.ComponentProps<'kbd'>) {
  return (
    <kbd
      data-slot="kbd"
      className={cn(
        "bg-muted text-muted-foreground pointer-events-none inline-flex h-5 w-fit min-w-5 items-center justify-center gap-1 rounded-sm px-1 font-sans text-xs font-medium select-none [&_svg:not([class*='size-'])]:size-3",
        className,
      )}
      {...props}
    />
  )
}

interface KbdHintProps {
  keys: ReadonlyArray<string>
  isMac?: boolean | null
  className?: string
}

function renderKey(key: string, isMac: boolean | null | undefined) {
  if (key === 'Mod') {
    if (isMac === null || isMac === undefined) return <span>Ctrl</span>
    return isMac ? <Command className="size-3" /> : <span>Ctrl</span>
  }
  return <span>{key}</span>
}

/**
 * Renders a sequence of keyboard keys, one chip per key, using the shadcn
 * {@link Kbd} component. The special `Mod` key resolves to ⌘ on macOS and
 * `Ctrl` elsewhere.
 */
export function KbdHint({ keys, isMac, className }: KbdHintProps) {
  return (
    <KbdGroup className={className}>
      {keys.map((key, index) => (
        <Kbd key={`${key}-${index}`}>{renderKey(key, isMac)}</Kbd>
      ))}
    </KbdGroup>
  )
}

export { Kbd, KbdGroup }
