/**
 * Versioned API prefix shared by every backend call. Bump it in this single
 * place (e.g. `/api/v2`) to target a new API version.
 *
 * `apiFetch` rewrites legacy `/api/...` paths to this prefix transparently, so
 * most call sites need no change. Direct callers that bypass `apiFetch`
 * (full-page OAuth/Steam redirects, server-side `fetch`) reference this constant
 * explicitly.
 */
export const API_PREFIX = '/api/v1'
