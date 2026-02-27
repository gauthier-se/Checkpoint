import { Link, createFileRoute } from '@tanstack/react-router'
import { RegisterForm } from '@/components/auth/register-form'

export const Route = createFileRoute('/_auth/register')({
  component: RegisterPage,
})

function RegisterPage() {
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
        <RegisterForm />
      </div>
    </div>
  )
}
