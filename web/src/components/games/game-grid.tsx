import type { Game } from '@/types/game'
import { GameCard } from './game-card'

interface GameGridProps {
  games: Game[]
  columns?: number
}

export function GameGrid({ games, columns = 8 }: GameGridProps) {
  const gridClass =
    columns === 7 ? 'grid grid-cols-7 gap-3' : 'grid grid-cols-8 gap-2'

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
