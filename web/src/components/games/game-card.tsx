import type { Game } from '@/types/game'
import { Link } from '@tanstack/react-router'

interface GameCardProps {
  game: Game
}

export function GameCard({ game }: GameCardProps) {
  return (
    <Link
      to="/games/$gameId"
      params={{ gameId: game.id }}
      className="group relative flex flex-col gap-2"
    >
      <img className="rounded-sm w-full" src={game.coverUrl} alt={game.title} />
      <div className="pointer-events-none absolute inset-0 rounded-sm bg-black/70 opacity-0 transition-opacity duration-200 group-hover:opacity-100" />
      <div className="pointer-events-none absolute inset-0 flex items-center justify-center px-2 text-center text-sm font-semibold opacity-0 transition-opacity duration-200 group-hover:opacity-100">
        {game.title}
      </div>
    </Link>
  )
}
