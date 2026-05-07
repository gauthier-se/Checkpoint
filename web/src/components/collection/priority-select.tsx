import type { Priority } from '@/types/collection'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

const NONE_VALUE = '__none__'

interface PrioritySelectProps {
  value: Priority | null
  onChange: (value: Priority | null) => void
  disabled?: boolean
}

export function PrioritySelect({
  value,
  onChange,
  disabled,
}: PrioritySelectProps) {
  return (
    <Select
      value={value ?? NONE_VALUE}
      onValueChange={(next) =>
        onChange(next === NONE_VALUE ? null : (next as Priority))
      }
      disabled={disabled}
    >
      <SelectTrigger size="sm" className="h-7 w-full text-xs">
        <SelectValue placeholder="Priority" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value={NONE_VALUE}>None</SelectItem>
        <SelectItem value="LOW">Low</SelectItem>
        <SelectItem value="MEDIUM">Medium</SelectItem>
        <SelectItem value="HIGH">High</SelectItem>
      </SelectContent>
    </Select>
  )
}
