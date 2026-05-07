import { Monitor, Moon, Sun } from 'lucide-react'
import { useTheme } from 'next-themes'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { cn } from '@/lib/utils'

const options = [
  { value: 'light', label: 'Light', Icon: Sun },
  { value: 'dark', label: 'Dark', Icon: Moon },
  { value: 'system', label: 'System', Icon: Monitor },
] as const

export function AppearanceSettings() {
  const { theme, setTheme } = useTheme()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  const current = mounted ? (theme ?? 'system') : null

  return (
    <Card>
      <CardHeader>
        <CardTitle>Appearance</CardTitle>
        <CardDescription>
          Customize how Checkpoint looks on your device.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div
          role="radiogroup"
          aria-label="Theme"
          className="grid max-w-md grid-cols-3 gap-2"
        >
          {options.map((opt) => {
            const active = current === opt.value
            return (
              <Button
                key={opt.value}
                type="button"
                variant={active ? 'default' : 'outline'}
                onClick={() => setTheme(opt.value)}
                aria-pressed={active}
                className={cn('flex h-auto flex-col items-center gap-2 py-4')}
              >
                <opt.Icon className="size-5" />
                <span>{opt.label}</span>
              </Button>
            )
          })}
        </div>
        <p className="text-muted-foreground mt-4 text-sm">
          System matches your device&apos;s appearance settings.
        </p>
      </CardContent>
    </Card>
  )
}
