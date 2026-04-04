export interface Tag {
  id: string
  name: string
  playLogsCount: number
}

export interface TagSummary {
  id: string
  name: string
}

export interface TagRequestDto {
  name: string
}
