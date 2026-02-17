import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { useAuth } from '@/hooks/use-auth'
import { createFileRoute } from '@tanstack/react-router'
import { Shield, User } from 'lucide-react'

export const Route = createFileRoute('/_app/_protected/profile')({
  component: ProfilePage,
})

function ProfilePage() {
  const { user } = useAuth()

  if (!user) return null

  return (
    <main className="max-w-7xl mx-auto py-10 px-4">
      <h1 className="text-3xl font-bold mb-8">My Profile</h1>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="size-5" />
              Account Information
            </CardTitle>
            <CardDescription>Your personal details</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground">Username</p>
              <p className="font-medium">{user.username}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Email</p>
              <p className="font-medium">{user.email}</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="size-5" />
              Role &amp; Permissions
            </CardTitle>
            <CardDescription>Your access level</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground">Role</p>
              <span className="inline-flex items-center rounded-full bg-primary/10 px-3 py-1 text-sm font-medium text-primary">
                {user.role}
              </span>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">User ID</p>
              <p className="font-mono text-sm text-muted-foreground">
                {user.id}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  )
}
