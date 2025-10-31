import { Button } from '@/components/ui/button'
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/')({ component: App })

function App() {
  return (
    <div className="bg-background gap-8 flex flex-col items-center justify-center min-h-screen p-4">
      <h1 className="text-3xl font-bold underline primary">Hello world!</h1>
      <Button>Button</Button>
    </div>
  )
}
