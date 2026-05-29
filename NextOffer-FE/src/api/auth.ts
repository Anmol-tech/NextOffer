import { apiFetch } from './client'
import type { AuthResponse, AuthUser } from './types'

export async function register(email: string, password: string, fullName: string) {
  return apiFetch<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password, fullName }),
  })
}

export async function login(email: string, password: string) {
  return apiFetch<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export async function getMe() {
  return apiFetch<AuthUser>('/api/auth/me')
}
