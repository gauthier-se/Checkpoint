import { LoginForm } from '@/components/auth/login-form'
import { createFileRoute, Link } from '@tanstack/react-router'

type LoginSearchParams = {
  redirect?: string
}

export const Route = createFileRoute('/_auth/login')({
  validateSearch: (search: Record<string, unknown>): LoginSearchParams => ({
    redirect: typeof search.redirect === 'string' ? search.redirect : undefined,
  }),
  component: LoginPage,
})

function LoginPage() {
  const { redirect } = Route.useSearch()

  return (
    <div className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
      <div className="flex w-full max-w-sm flex-col gap-6">
        <Link
          to="/"
          className="flex items-center gap-2 self-center font-medium"
        >
          <img className="w-6" src="/images/logo.png" alt="Checkpoint" />
          Checkpoint
        </Link>
        <LoginForm redirectTo={redirect} />
      </div>
    </div>
  )
}
