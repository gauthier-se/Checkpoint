import { Outlet, createFileRoute } from '@tanstack/react-router'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useNotificationsWebSocket } from '@/hooks/use-notifications-websocket'

export const Route = createFileRoute('/_app')({
  component: AppLayout,
})

function AppLayout() {
  useNotificationsWebSocket()

  return (
    <>
      <Header />
      <Outlet />
      <Footer />
    </>
  )
}
