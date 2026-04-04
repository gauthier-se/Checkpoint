import { Link, createFileRoute } from '@tanstack/react-router'
import { Archive, BookOpen, Heart, Library, Tag } from 'lucide-react'
import type { CollectionTab } from '@/types/collection'
import { BacklogTab, backlogQuery } from '@/components/collection/backlog-tab'
import { LibraryTab, libraryQuery } from '@/components/collection/library-tab'
import { PlayLogTab, playLogQuery } from '@/components/collection/play-log-tab'
import {
  WishlistTab,
  wishlistQuery,
} from '@/components/collection/wishlist-tab'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'

// Search params

const VALID_TABS: Array<CollectionTab> = [
  'library',
  'wishlist',
  'backlog',
  'playlog',
]

type GamesSearchParams = {
  tab: CollectionTab
  page: number
}

// Route

export const Route = createFileRoute('/_app/_protected/$username/games/')({
  component: UserGamesPage,
  validateSearch: (search: Record<string, unknown>): GamesSearchParams => {
    const rawTab = String(search.tab ?? 'library')
    const tab = VALID_TABS.includes(rawTab as CollectionTab)
      ? (rawTab as CollectionTab)
      : 'library'
    const page = Math.max(1, Math.floor(Number(search.page ?? 1)) || 1)
    return { tab, page }
  },
  loaderDeps: ({ search: { tab, page } }) => ({ tab, page }),
  loader: async ({ context, deps: { tab, page } }) => {
    // Prefetch data for the active tab; wrapped in try/catch so
    // missing API endpoints (not yet merged) don't crash navigation
    try {
      switch (tab) {
        case 'library':
          await context.queryClient.ensureQueryData(libraryQuery(page))
          break
        case 'wishlist':
          await context.queryClient.ensureQueryData(wishlistQuery(page))
          break
        case 'backlog':
          await context.queryClient.ensureQueryData(backlogQuery(page))
          break
        case 'playlog':
          await context.queryClient.ensureQueryData(playLogQuery(page))
          break
      }
    } catch {
      // Silently ignore — the tab component will show the error inline
    }
  },
})

// Tab config

const TAB_CONFIG: Array<{
  value: CollectionTab
  label: string
  icon: React.ReactNode
}> = [
  { value: 'library', label: 'Library', icon: <Library className="size-4" /> },
  { value: 'wishlist', label: 'Wishlist', icon: <Heart className="size-4" /> },
  { value: 'backlog', label: 'Backlog', icon: <Archive className="size-4" /> },
  {
    value: 'playlog',
    label: 'Play Log',
    icon: <BookOpen className="size-4" />,
  },
]

// Page

function UserGamesPage() {
  const { username } = Route.useParams()
  const { tab, page } = Route.useSearch()
  const navigate = Route.useNavigate()

  function onTabChange(newTab: string) {
    void navigate({
      search: { tab: newTab as CollectionTab, page: 1 },
    })
  }

  return (
    <main className="mx-auto max-w-7xl px-4 py-10">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold">My Games</h1>
        <Link
          to="/$username/tags"
          params={{ username }}
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <Tag className="size-4" />
          Manage Tags
        </Link>
      </div>

      <Tabs value={tab} onValueChange={onTabChange}>
        <TabsList variant="line" className="mb-6 w-full justify-start">
          {TAB_CONFIG.map(({ value, label, icon }) => (
            <TabsTrigger key={value} value={value} className="gap-2 px-4 py-2">
              {icon}
              {label}
            </TabsTrigger>
          ))}
        </TabsList>

        <TabsContent value="library">
          <LibraryTab page={tab === 'library' ? page : 1} />
        </TabsContent>

        <TabsContent value="wishlist">
          <WishlistTab page={tab === 'wishlist' ? page : 1} />
        </TabsContent>

        <TabsContent value="backlog">
          <BacklogTab page={tab === 'backlog' ? page : 1} />
        </TabsContent>

        <TabsContent value="playlog">
          <PlayLogTab page={tab === 'playlog' ? page : 1} />
        </TabsContent>
      </Tabs>
    </main>
  )
}
