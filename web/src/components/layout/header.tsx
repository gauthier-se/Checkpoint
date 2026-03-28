import { useState } from 'react'
import { Link } from '@tanstack/react-router'
import { useHotkey } from '@tanstack/react-hotkeys'
import { Command, LogIn, Plus, Search } from 'lucide-react'
import { Button } from '../ui/button'
import { SearchCommand } from '../search/search-command'
import { AvatarDropdown } from './avatar-dropdown'
import { useAuth } from '@/hooks/use-auth'

function useIsMac() {
  if (typeof navigator === 'undefined') return true
  return /Mac|iPod|iPhone|iPad/.test(navigator.userAgent)
}

export const Header = () => {
  const { user, isLoading } = useAuth()
  const [searchOpen, setSearchOpen] = useState(false)
  const isMac = useIsMac()

  useHotkey('Mod+K', () => {
    setSearchOpen((prev) => !prev)
  })

  return (
    <header className="max-w-7xl mx-auto py-4 flex items-center justify-between">
      <Link className="flex items-center gap-2" to="/">
        <img className="w-8 pt-1" src="/images/logo.png" alt="" />
        <h1 className="text-2xl font-bold">Checkpoint</h1>
      </Link>
      <nav className="flex items-center gap-4 pt-2">
        <Link
          to="/games"
          search={{ page: 1 }}
          className="text-muted-foreground font-semibold"
        >
          Games
        </Link>
        <Link
          to="/members"
          search={{ page: 1 }}
          className="text-muted-foreground font-semibold"
        >
          Members
        </Link>
        <button
          onClick={() => setSearchOpen(true)}
          className="flex h-9 items-center gap-2 rounded-lg border bg-muted/50 px-3 text-sm text-muted-foreground transition-colors hover:bg-muted"
        >
          <Search className="size-4 shrink-0" />
          <span>Search...</span>
          <kbd className="pointer-events-none hidden select-none items-center gap-0.5 rounded bg-muted px-1.5 py-0.5 font-mono text-[11px] font-medium text-muted-foreground sm:inline-flex">
            {isMac ? <Command className="size-2.5" /> : <span>Ctrl+</span>}
            <span>K</span>
          </kbd>
        </button>
        <SearchCommand open={searchOpen} onOpenChange={setSearchOpen} />
        <Button asChild size="sm">
          <Link to="/" className="text-muted-foreground font-semibold">
            <Plus />
            Log
          </Link>
        </Button>
        {!isLoading &&
          (user ? (
            <AvatarDropdown user={user} />
          ) : (
            <Button asChild variant="ghost" size="sm">
              <Link to="/login" className="text-muted-foreground font-semibold">
                <LogIn />
                Sign in
              </Link>
            </Button>
          ))}
      </nav>
    </header>
  )
}
