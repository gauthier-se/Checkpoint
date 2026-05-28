import { useHotkeySequence } from '@tanstack/react-hotkeys'
import { toast } from 'sonner'
import { triggerKonami } from '@/queries/easter-eggs'

const KONAMI_SEQUENCE = [
  'ArrowUp',
  'ArrowUp',
  'ArrowDown',
  'ArrowDown',
  'ArrowLeft',
  'ArrowRight',
  'ArrowLeft',
  'ArrowRight',
  'B',
  'A',
] as const

/**
 * Listens for the Konami code anywhere in the app. Mounted once at the layout
 * level so the easter egg fires on every page, not only the home route.
 *
 * <p>1500 ms between keys leaves room for the full ten-key sequence without
 * letting a stray match linger forever between presses.</p>
 */
export function useKonamiHotkey() {
  useHotkeySequence(
    KONAMI_SEQUENCE,
    () => {
      toast.success('You found it!')
      void triggerKonami()
    },
    { timeout: 1500 },
  )
}
