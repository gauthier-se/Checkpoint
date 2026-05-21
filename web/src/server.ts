import {
  createStartHandler,
  defaultStreamHandler,
} from '@tanstack/react-start/server'
import type { HandlerCallback } from '@tanstack/react-start/server'
import type { Register } from '@tanstack/react-router'

// Nitro's Vite plugin (`nitro/dist/_build/vite.plugin.mjs`) drops any SSR
// response whose `status === 404` and falls through to Vite's connect
// middleware, which then serves the raw `Cannot GET /url` page from
// `finalhandler`. That swallows the React `notFoundComponent` defined in
// `src/routes/__root.tsx`. As a workaround, we rewrap 404 responses with
// status 200 so Nitro forwards them; the HTML body still tells the user
// the page wasn't found via the ErrorPage component.
const handlerWithForwarded404: HandlerCallback<Register['router']> = async (
  ctx,
) => {
  const response = await defaultStreamHandler(ctx)
  if (response.status !== 404) return response
  return new Response(response.body, {
    status: 200,
    statusText: 'OK',
    headers: response.headers,
  })
}

const fetch = createStartHandler(handlerWithForwarded404)

export default { fetch }
