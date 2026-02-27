import { useFormStatus } from 'react-dom'
import { Button } from '@/components/ui/button'

interface SubmitButtonProps {
  label?: string
  pendingLabel?: string
}

export function SubmitButton({
  label = 'Login',
  pendingLabel = 'Signing in...',
}: SubmitButtonProps) {
  const { pending } = useFormStatus()

  return (
    <Button type="submit" disabled={pending}>
      {pending ? pendingLabel : label}
    </Button>
  )
}
