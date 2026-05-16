import { Component } from 'react'
import type { ErrorInfo, ReactNode } from 'react'
import { ErrorPage } from '@/components/errors/error-page'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
}

/**
 * Catches render-time errors that escape TanStack Router's per-route
 * `errorComponent` and React Query's error states (e.g. throws in a
 * component body, in a memoized selector, etc.). Acts as the final
 * safety net so a thrown error never leaves the user with a blank page.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('ErrorBoundary caught:', error, info)
  }

  handleReset = () => {
    this.setState({ hasError: false })
  }

  render() {
    if (this.state.hasError) {
      return <ErrorPage onRetry={this.handleReset} />
    }
    return this.props.children
  }
}
