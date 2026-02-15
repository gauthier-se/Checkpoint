import { Button } from '@/components/ui/button'
import { apiFetch } from '@/services/api'
import { createFileRoute } from '@tanstack/react-router'
import { useState } from 'react'

export const Route = createFileRoute('/_app/')({ component: App })

function App() {
  const [pong, setPong] = useState('')

  const handlePing = async () => {
    const res = await apiFetch('/ping')
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
