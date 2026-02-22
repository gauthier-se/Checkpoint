import { Link } from '@tanstack/react-router'
import { LogIn, Plus, Search } from 'lucide-react'
import { Button } from '../ui/button'
import { AvatarDropdown } from './avatar-dropdown'
import { useAuth } from '@/hooks/use-auth'

export const Header = () => {
  const { user, isLoading } = useAuth()

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
        <Link to="/" className="text-muted-foreground font-semibold">
          Members
        </Link>
        <Link to="/" className="text-muted-foreground font-semibold">
          <Search />
        </Link>
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
