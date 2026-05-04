import { Link, useNavigate } from '@tanstack/react-router'
import type { User } from '@/types/user'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useAuth } from '@/hooks/use-auth'

const API_URL = import.meta.env.VITE_API_URL

export function AvatarDropdown({ user }: { user: User }) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const initials = user.username.slice(0, 2).toUpperCase()
  const avatarSrc = user.picture ? `${API_URL}${user.picture}` : undefined

  const handleLogout = async () => {
    await logout()
    await navigate({ to: '/' })
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="rounded-full">
          <Avatar>
            <AvatarImage src={avatarSrc} alt={user.username} />
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-32">
        <DropdownMenuGroup>
          <DropdownMenuItem asChild>
            <Link to="/profile">Profile</Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <Link
              to="/$username/games"
              params={{ username: user.username }}
              search={{ tab: 'library', page: 1 }}
            >
              My Games
            </Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <Link to="/lists" search={{ page: 1 }}>
              My Lists
            </Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <Link to="/settings/profile">Settings</Link>
          </DropdownMenuItem>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          <DropdownMenuItem variant="destructive" onClick={handleLogout}>
            Log out
          </DropdownMenuItem>
        </DropdownMenuGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
