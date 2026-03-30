import { createFileRoute } from '@tanstack/react-router'
import { Heart, List, MessageSquare, Users } from 'lucide-react'
import type { UserProfile } from '@/types/profile'
import {
  userFollowingQueryOptions,
  userProfileQueryOptions,
  userReviewsQueryOptions,
  userWishlistQueryOptions,
} from '@/queries/profile'
import { userListsQueryOptions } from '@/queries/lists'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ProfileHeader } from '@/components/profile/profile-header'
import { ProfileReviewsTab } from '@/components/profile/profile-reviews-tab'
import { ProfileWishlistTab } from '@/components/profile/profile-wishlist-tab'
import { ProfileFollowingTab } from '@/components/profile/profile-following-tab'
import { ProfileListsTab } from '@/components/profile/profile-lists-tab'

// Search params

type ProfileTab = 'reviews' | 'wishlist' | 'lists' | 'following'

const VALID_TABS: Array<ProfileTab> = ['reviews', 'wishlist', 'lists', 'following']

type ProfileSearchParams = {
  tab: ProfileTab
  page: number
}

// Route

export const Route = createFileRoute('/_app/profile/$username')({
  component: UserProfilePage,
  validateSearch: (search: Record<string, unknown>): ProfileSearchParams => {
    const rawTab = String(search.tab ?? 'reviews')
    const tab = VALID_TABS.includes(rawTab as ProfileTab)
      ? (rawTab as ProfileTab)
      : 'reviews'
    const page = Math.max(1, Math.floor(Number(search.page ?? 1)) || 1)
    return { tab, page }
  },
  loaderDeps: ({ search: { tab, page } }) => ({ tab, page }),
  loader: async ({ params: { username }, context, deps: { tab, page } }) => {
    const profile = await context.queryClient.ensureQueryData(
      userProfileQueryOptions(username),
    )

    // Prefetch active tab data
    const apiPage = Math.max(0, page - 1)
    try {
      switch (tab) {
        case 'reviews':
          void context.queryClient.prefetchQuery(
            userReviewsQueryOptions(username, apiPage),
          )
          break
        case 'wishlist':
          void context.queryClient.prefetchQuery(
            userWishlistQueryOptions(username, apiPage),
          )
          break
        case 'lists':
          void context.queryClient.prefetchQuery(
            userListsQueryOptions(username, apiPage),
          )
          break
        case 'following':
          void context.queryClient.prefetchQuery(
            userFollowingQueryOptions(profile.id, apiPage),
          )
          break
      }
    } catch {
      // Silently ignore — tab component handles errors inline
    }

    return profile
  },
})

// Tab config

const TAB_CONFIG: Array<{
  value: ProfileTab
  label: string
  icon: React.ReactNode
}> = [
  {
    value: 'reviews',
    label: 'Reviews',
    icon: <MessageSquare className="size-4" />,
  },
  {
    value: 'wishlist',
    label: 'Wishlist',
    icon: <Heart className="size-4" />,
  },
  {
    value: 'lists',
    label: 'Lists',
    icon: <List className="size-4" />,
  },
  {
    value: 'following',
    label: 'Following',
    icon: <Users className="size-4" />,
  },
]

// Page

function UserProfilePage() {
  const profile: UserProfile = Route.useLoaderData()
  const { tab, page } = Route.useSearch()
  const navigate = Route.useNavigate()

  function onTabChange(newTab: string) {
    void navigate({
      search: { tab: newTab as ProfileTab, page: 1 },
    })
  }

  return (
    <main className="mx-auto max-w-7xl px-4 py-10">
      <ProfileHeader profile={profile} />

      <Tabs value={tab} onValueChange={onTabChange} className="mt-6">
        <TabsList variant="line" className="mb-6 w-full justify-start">
          {TAB_CONFIG.map(({ value, label, icon }) => (
            <TabsTrigger key={value} value={value} className="gap-2 px-4 py-2">
              {icon}
              {label}
            </TabsTrigger>
          ))}
        </TabsList>

        <TabsContent value="reviews">
          <ProfileReviewsTab
            profile={profile}
            page={tab === 'reviews' ? page : 1}
          />
        </TabsContent>

        <TabsContent value="wishlist">
          <ProfileWishlistTab
            profile={profile}
            page={tab === 'wishlist' ? page : 1}
          />
        </TabsContent>

        <TabsContent value="lists">
          <ProfileListsTab
            profile={profile}
            page={tab === 'lists' ? page : 1}
          />
        </TabsContent>

        <TabsContent value="following">
          <ProfileFollowingTab
            profile={profile}
            page={tab === 'following' ? page : 1}
          />
        </TabsContent>
      </Tabs>
    </main>
  )
}
