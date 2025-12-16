import { Button } from '@/components/ui/button'
import { createFileRoute } from '@tanstack/react-router'
import { useState } from 'react'

export const Route = createFileRoute('/')({ component: App })

function App() {
  const [pong, setPong] = useState('')

  const handlePing = async () => {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/ping`)
    const data = await res.json()
    setPong(data.message)
  }

  return (
    <div className="bg-background gap-8 flex flex-col items-center justify-center min-h-screen p-4">
      <Button onClick={handlePing}>Ping</Button>
      {pong && <p>{pong}</p>}
    </div>
  )
}
