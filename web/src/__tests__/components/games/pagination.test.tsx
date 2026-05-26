import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import type { ComponentProps, ReactNode } from 'react'

// Imported after the mock so the component picks up the mocked Link.
import { GamesPagination } from '@/components/games/pagination'

vi.mock('@tanstack/react-router', () => ({
  // Replace TanStack Router's <Link> with a plain <a> so the component can
  // render outside of a router context. Router-only props (to/search/hash) are
  // dropped before spreading; everything else (className, aria-*, data-*) is
  // forwarded so the shadcn pagination styling/state attributes survive.
  Link: ({
    children,
    disabled,
    to,
    search,
    hash,
    ...rest
  }: {
    children: ReactNode
    disabled?: boolean
    to?: string
    search?: unknown
    hash?: string
  } & ComponentProps<'a'>) => (
    <a
      href={typeof to === 'string' ? to : '#'}
      data-disabled={disabled ? 'true' : undefined}
      {...rest}
    >
      {children}
    </a>
  ),
}))

describe('GamesPagination', () => {
  it('renders one page link per page when totalPages <= 7', () => {
    render(
      <GamesPagination
        page={1}
        totalPages={5}
        hasNext
        hasPrevious={false}
        search={{}}
      />,
    )

    for (const label of ['1', '2', '3', '4', '5']) {
      expect(screen.getByRole('link', { name: label })).toBeInTheDocument()
    }
    // No ellipsis links.
    expect(screen.queryByRole('link', { name: '...' })).toBeNull()
  })

  it('marks only the current page link with aria-current', () => {
    render(
      <GamesPagination
        page={3}
        totalPages={5}
        hasNext
        hasPrevious
        search={{}}
      />,
    )

    const current = screen.getByRole('link', { name: '3' })
    const other = screen.getByRole('link', { name: '2' })
    expect(current).toHaveAttribute('aria-current', 'page')
    expect(other).not.toHaveAttribute('aria-current')
  })

  it('renders Previous and Next as non-link disabled controls at the boundaries', () => {
    render(
      <GamesPagination
        page={1}
        totalPages={3}
        hasNext={false}
        hasPrevious={false}
        search={{}}
      />,
    )

    // Disabled edges are not navigable links...
    expect(screen.queryByRole('link', { name: /previous/i })).toBeNull()
    expect(screen.queryByRole('link', { name: /next/i })).toBeNull()
    // ...but they are present and flagged as disabled.
    expect(
      screen.getByText('Previous').closest('[aria-disabled]'),
    ).not.toBeNull()
    expect(screen.getByText('Next').closest('[aria-disabled]')).not.toBeNull()
  })
})
