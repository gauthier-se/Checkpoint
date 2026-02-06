import { Link } from '@tanstack/react-router'
import { Plus, Search } from 'lucide-react'
import { Button } from '../ui/button'
import { AvatarDropdown } from './avatar-dropdown'

export const Header = () => {
  return (
    <header className="max-w-7xl mx-auto py-4 flex items-center justify-between">
      <Link className="flex items-center gap-2" to="/">
        <img className="w-8 pt-1" src="./images/logo.png" alt="" />
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
        <Link to="/" className="text-muted-foreground font-semibold">
          <Button size="sm">
            <Plus />
            Log
          </Button>
        </Link>
        <AvatarDropdown />
      </nav>
    </header>
  )
}
