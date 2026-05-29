import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import * as authApi from '../api/auth'
import { ApiError, getStoredToken, setStoredToken } from '../api/client'
import type { AuthUser } from '../api/types'

type AuthContextValue = {
  user: AuthUser | null
  token: string | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, fullName: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false

    async function bootstrap() {
      const stored = getStoredToken()
      if (!stored) {
        if (!cancelled) {
          setLoading(false)
        }
        return
      }

      try {
        const me = await authApi.getMe()
        if (!cancelled) {
          setUser(me)
          setToken(stored)
        }
      } catch {
        setStoredToken(null)
        if (!cancelled) {
          setUser(null)
          setToken(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void bootstrap()
    return () => {
      cancelled = true
    }
  }, [])

  const applySession = useCallback((response: { token: string; user: AuthUser }) => {
    setStoredToken(response.token)
    setToken(response.token)
    setUser(response.user)
  }, [])

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await authApi.login(email, password)
      applySession(response)
    },
    [applySession],
  )

  const register = useCallback(
    async (email: string, password: string, fullName: string) => {
      const response = await authApi.register(email, password, fullName)
      applySession(response)
    },
    [applySession],
  )

  const logout = useCallback(() => {
    setStoredToken(null)
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, token, loading, login, register, logout }),
    [user, token, loading, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}

export function formatAuthError(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'Something went wrong'
}
