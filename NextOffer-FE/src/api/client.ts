import type { ApiProblem } from './types'

const TOKEN_KEY = 'nextoffer_token'

/** Empty in dev (Vite proxy); set VITE_API_BASE_URL in production (e.g. Render). */
export function apiUrl(path: string): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? ''
  return `${base}${path}`
}

export class ApiError extends Error {
  status: number
  body: ApiProblem | null

  constructor(status: number, message: string, body: ApiProblem | null = null) {
    super(message)
    this.status = status
    this.body = body
  }
}

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (!headers.has('Content-Type') && init.body) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getStoredToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(apiUrl(path), { ...init, headers })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const data = text ? (JSON.parse(text) as unknown) : null

  if (!response.ok) {
    const problem = data as ApiProblem | null
    throw new ApiError(
      response.status,
      problem?.detail ?? problem?.title ?? `Request failed (${response.status})`,
      problem,
    )
  }

  return data as T
}
