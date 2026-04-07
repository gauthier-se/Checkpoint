import { GameCard } from './game-card'
import type { Game } from '@/types/game'

interface GameGridProps {
  games: Array<Game>
  columns?: number
}

export function GameGrid({ games, columns = 8 }: GameGridProps) {
  const gridClass =
    columns === 7
      ? 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-7 gap-3'
      : 'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-8 gap-2'

  return (
    <div className={`${gridClass} py-4`}>
      {games.map((game) => (
        <div key={game.id}>
          <GameCard game={game} />
        </div>
      ))}
    </div>
  )
}
