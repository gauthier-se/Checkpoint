import { Link, createFileRoute } from '@tanstack/react-router'
import { Gamepad2, ListChecks, Star, Users } from 'lucide-react'
import type { Game } from '@/types/game'
import type { NewsArticle } from '@/types/news'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { GameGrid } from '@/components/games/game-grid'
import { NewsCard } from '@/components/news/news-card'
import { useAuth } from '@/hooks/use-auth'
import { trendingGamesQueryOptions } from '@/queries/catalog'
import { newsListQueryOptions } from '@/queries/news'

interface HomeData {
  trending: Array<Game>
  news: Array<NewsArticle>
}

export const Route = createFileRoute('/_app/')({
  component: App,
  loader: async ({ context }): Promise<HomeData> => {
    const [trending, newsResponse] = await Promise.all([
      context.queryClient.ensureQueryData(trendingGamesQueryOptions()),
      context.queryClient.ensureQueryData(newsListQueryOptions(0, 3)),
    ])
    return { trending, news: newsResponse.content }
  },
})

function App() {
  const { user } = useAuth()
  const data = Route.useLoaderData()

  if (user) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <p className="text-muted-foreground">
          Welcome back, {user.pseudo}! Authenticated homepage coming soon.
        </p>
      </div>
    )
  }

  return (
    <div>
      <HeroSection />
      <div className="mx-auto max-w-7xl px-4">
        <TrendingSection games={data.trending} />
        <FeaturesSection />
        <NewsSection articles={data.news} />
      </div>
      <CtaSection />
    </div>
  )
}

function HeroSection() {
  return (
    <section className="py-24 sm:py-32">
      <div className="mx-auto max-w-4xl px-4 text-center">
        <h1 className="text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl">
          Your gaming journey,{' '}
          <span className="text-primary">all in one place</span>
        </h1>
        <p className="mx-auto mt-6 max-w-2xl text-lg text-muted-foreground sm:text-xl">
          Track your game library, rate and review titles, create curated lists,
          and share your gaming experiences with friends.
        </p>
        <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
          <Button asChild size="lg">
            <Link to="/register">Join CheckPoint</Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link to="/login">Sign in</Link>
          </Button>
        </div>
      </div>
    </section>
  )
}

function TrendingSection({ games }: { games: Array<Game> }) {
  if (games.length === 0) return null

  return (
    <section className="my-12">
      <div className="flex items-center justify-between py-2">
        <h2 className="text-muted-foreground font-semibold">
          Popular games this week
        </h2>
        <Link
          to="/games"
          search={{ page: 1 }}
          className="text-sm text-muted-foreground hover:text-foreground"
        >
          See all
        </Link>
      </div>
      <Separator />
      <GameGrid games={games} columns={7} />
    </section>
  )
}

const features = [
  {
    icon: Gamepad2,
    title: 'Track your library',
    description:
      'Keep a record of every game you own, are playing, or want to play.',
  },
  {
    icon: Star,
    title: 'Rate and review',
    description:
      'Share your opinions and help others discover their next favorite game.',
  },
  {
    icon: ListChecks,
    title: 'Create curated lists',
    description:
      'Organize games into themed collections and share them with the community.',
  },
  {
    icon: Users,
    title: 'Connect with friends',
    description:
      'Follow other players, see what they are playing, and discover new games.',
  },
] as const

function FeaturesSection() {
  return (
    <section className="my-12">
      <div className="py-2">
        <h2 className="text-muted-foreground font-semibold">
          Everything you need
        </h2>
      </div>
      <Separator />
      <div className="grid grid-cols-1 gap-6 py-8 sm:grid-cols-2 lg:grid-cols-4">
        {features.map((feature) => (
          <div
            key={feature.title}
            className="flex flex-col items-center gap-3 rounded-lg border p-6 text-center"
          >
            <div className="flex size-12 items-center justify-center rounded-full bg-primary/10">
              <feature.icon className="size-6 text-primary" />
            </div>
            <h3 className="font-semibold">{feature.title}</h3>
            <p className="text-sm text-muted-foreground">
              {feature.description}
            </p>
          </div>
        ))}
      </div>
    </section>
  )
}

function NewsSection({ articles }: { articles: Array<NewsArticle> }) {
  if (articles.length === 0) return null

  return (
    <section className="my-12">
      <div className="flex items-center justify-between py-2">
        <h2 className="text-muted-foreground font-semibold">Latest news</h2>
        <Link
          to="/news"
          search={{ page: 1 }}
          className="text-sm text-muted-foreground hover:text-foreground"
        >
          See all
        </Link>
      </div>
      <Separator />
      <div className="grid grid-cols-1 gap-4 py-4 sm:grid-cols-2 lg:grid-cols-3">
        {articles.map((article) => (
          <NewsCard key={article.id} article={article} />
        ))}
      </div>
    </section>
  )
}

function CtaSection() {
  return (
    <section className="border-t py-16">
      <div className="mx-auto max-w-2xl px-4 text-center">
        <h2 className="text-2xl font-bold sm:text-3xl">Join the community</h2>
        <p className="mt-4 text-muted-foreground">
          Start tracking your games and connect with other players today.
        </p>
        <Button asChild size="lg" className="mt-8">
          <Link to="/register">Create your account</Link>
        </Button>
      </div>
    </section>
  )
}
