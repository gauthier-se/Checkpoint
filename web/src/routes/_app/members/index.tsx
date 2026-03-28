import { useDeferredValue, useEffect, useState } from 'react'
import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { useQuery, useSuspenseQuery } from '@tanstack/react-query'
import { Loader2, Search, Users, X } from 'lucide-react'
import type { MembersResponse, MembersSearchParams } from '@/types/member'
import { MemberCard } from '@/components/members/member-card'
import { MembersPagination } from '@/components/members/members-pagination'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { apiFetch } from '@/services/api'
import {
  popularMembersQueryOptions,
  topReviewersMembersQueryOptions,
  suggestedMembersQueryOptions,
  browseMembersQueryOptions,
} from '@/queries/members'
import { useAuth } from '@/hooks/use-auth'

const PAGE_SIZE = 20

function parseOptionalString(value: unknown): string | undefined {
  return typeof value === 'string' && value.length > 0 ? value : undefined
}

function buildBrowseUrl(params: MembersSearchParams): string {
  const qs = new URLSearchParams()
  qs.set('page', String(params.page - 1))
  qs.set('size', String(PAGE_SIZE))
  if (params.search) qs.set('search', params.search)
  return `/api/members?${qs.toString()}`
}

export const Route = createFileRoute('/_app/members/')({
  component: RouteComponent,
  validateSearch: (search: Record<string, unknown>): MembersSearchParams => ({
    page: Math.max(1, Math.floor(Number(search.page ?? 1)) || 1),
    search: parseOptionalString(search.search),
  }),
  loaderDeps: ({ search }) => search,
  loader: async ({ deps, context }): Promise<MembersResponse> => {
    const [data] = await Promise.all([
      apiFetch(buildBrowseUrl(deps)).then(
        (res): Promise<MembersResponse> => res.json(),
      ),
      context.queryClient.ensureQueryData(popularMembersQueryOptions(10)),
      context.queryClient.ensureQueryData(topReviewersMembersQueryOptions(10)),
    ])
    return data
  },
})

function MemberSection({
  title,
  children,
}: {
  title: string
  children: React.ReactNode
}) {
  return (
    <div className="my-8">
      <h2 className="py-2 text-muted-foreground font-semibold">{title}</h2>
      <Separator />
      {children}
    </div>
  )
}

function RouteComponent() {
  const data = Route.useLoaderData()
  const searchParams = Route.useSearch()
  const { page, search: urlSearch } = searchParams
  const navigate = useNavigate({ from: '/members' })
  const { user } = useAuth()

  const [inputValue, setInputValue] = useState(urlSearch ?? '')
  const deferredQuery = useDeferredValue(inputValue)

  const isSearchActive = deferredQuery.length >= 2

  const { data: popularMembers } = useSuspenseQuery(
    popularMembersQueryOptions(10),
  )
  const { data: topReviewers } = useSuspenseQuery(
    topReviewersMembersQueryOptions(10),
  )
  const { data: suggestedMembers } = useQuery({
    ...suggestedMembersQueryOptions(10),
    enabled: user !== null,
  })

  // Sync deferred query to URL
  useEffect(() => {
    const urlQ = deferredQuery.length >= 2 ? deferredQuery : undefined
    if (urlQ !== urlSearch) {
      navigate({
        search: (prev) => ({ ...prev, search: urlQ, page: 1 }),
        replace: true,
      })
    }
  }, [deferredQuery, urlSearch, navigate])

  // Sync URL search to input when navigating directly
  useEffect(() => {
    if (urlSearch && urlSearch !== inputValue) {
      setInputValue(urlSearch)
    }
  }, [urlSearch])

  function clearSearch() {
    setInputValue('')
    navigate({
      search: (prev) => ({ ...prev, search: undefined, page: 1 }),
      replace: true,
    })
  }

  return (
    <div className="max-w-7xl mx-auto">
      {/* Search bar */}
      <div className="mt-10 py-2 text-muted-foreground font-semibold flex items-center justify-between">
        <h1 className="text-xl font-bold text-foreground">Members</h1>
        <div className="flex items-center gap-4">
          <p className="min-w-fit">Find a member</p>
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
            <Input
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder="Search by pseudo..."
              className="pl-8 pr-8"
            />
            {inputValue.length > 0 && (
              <button
                type="button"
                onClick={clearSearch}
                className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="size-4" />
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Curated sections — hidden when searching */}
      {!isSearchActive && (
        <>
          {popularMembers.length > 0 && (
            <MemberSection title="Popular Members">
              <div className="grid grid-cols-2 gap-4 py-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {popularMembers.map((member) => (
                  <MemberCard key={member.id} member={member} />
                ))}
              </div>
            </MemberSection>
          )}

          {topReviewers.length > 0 && (
            <MemberSection title="Top Reviewers">
              <div className="grid grid-cols-2 gap-4 py-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {topReviewers.map((member) => (
                  <MemberCard key={member.id} member={member} />
                ))}
              </div>
            </MemberSection>
          )}

          {user && suggestedMembers && suggestedMembers.length > 0 && (
            <MemberSection title="Suggested Members">
              <div className="grid grid-cols-2 gap-4 py-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {suggestedMembers.map((member) => (
                  <MemberCard key={member.id} member={member} />
                ))}
              </div>
            </MemberSection>
          )}
        </>
      )}

      {/* Browse / Search results */}
      <MemberSection
        title={
          isSearchActive
            ? `Results for "${deferredQuery}"`
            : data.metadata.totalElements === 0
              ? 'No members found'
              : `${data.metadata.totalElements} member${data.metadata.totalElements > 1 ? 's' : ''}`
        }
      >
        {data.content.length > 0 ? (
          <>
            <div className="grid grid-cols-2 gap-4 py-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
              {data.content.map((member) => (
                <MemberCard key={member.id} member={member} />
              ))}
            </div>
            <MembersPagination
              page={page}
              totalPages={data.metadata.totalPages}
              hasNext={data.metadata.hasNext}
              hasPrevious={data.metadata.hasPrevious}
              search={searchParams}
            />
          </>
        ) : (
          <div className="flex flex-col items-center gap-3 py-12 text-center">
            <Users className="text-muted-foreground size-12" />
            <p className="text-muted-foreground text-lg">
              {isSearchActive
                ? `No members found for "${deferredQuery}"`
                : 'No members to display'}
            </p>
          </div>
        )}
      </MemberSection>
    </div>
  )
}
