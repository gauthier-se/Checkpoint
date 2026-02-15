const API_URL = import.meta.env.VITE_API_URL

/**
 * Wrapper around fetch that automatically includes credentials (cookies)
 * for session-based authentication.
 */
export async function apiFetch(
  path: string,
  init?: RequestInit,
): Promise<Response> {
  return fetch(`${API_URL}${path}`, {
    ...init,
    credentials: 'include',
  })
}
