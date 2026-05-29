import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import * as jobsApi from '../api/jobs'
import * as watchesApi from '../api/watches'
import { mapJobPosting } from '../lib/mappers'
import type { CompanyWatch } from '../api/types'
import type { Job } from '../types'
import { useAuth } from './AuthContext'

type AppDataContextValue = {
  jobs: Job[]
  watches: CompanyWatch[]
  loading: boolean
  error: string | null
  refreshJobs: () => Promise<void>
  refreshWatches: () => Promise<void>
  addWatch: (companyName: string, careerPageUrl: string) => Promise<void>
  removeWatch: (id: number) => Promise<void>
  pollWatch: (id: number) => Promise<number>
  pollAllWatches: () => Promise<void>
}

const AppDataContext = createContext<AppDataContextValue | null>(null)

export function AppDataProvider({ children }: { children: ReactNode }) {
  const { token } = useAuth()
  const [jobs, setJobs] = useState<Job[]>([])
  const [watches, setWatches] = useState<CompanyWatch[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const refreshJobs = useCallback(async () => {
    const postings = await jobsApi.listJobs()
    setJobs(postings.map(mapJobPosting))
  }, [])

  const refreshWatches = useCallback(async () => {
    const list = await watchesApi.listWatches()
    setWatches(list)
  }, [])

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      await Promise.all([refreshJobs(), refreshWatches()])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data')
    } finally {
      setLoading(false)
    }
  }, [refreshJobs, refreshWatches])

  useEffect(() => {
    if (token) {
      void loadAll()
    } else {
      setJobs([])
      setWatches([])
      setError(null)
    }
  }, [token, loadAll])

  const addWatch = useCallback(
    async (companyName: string, careerPageUrl: string) => {
      await watchesApi.createWatch({
        companyName,
        careerPageUrl,
        atsType: 'GREENHOUSE',
        enabled: true,
      })
      await loadAll()
    },
    [loadAll],
  )

  const removeWatch = useCallback(
    async (id: number) => {
      await watchesApi.deleteWatch(id)
      await loadAll()
    },
    [loadAll],
  )

  const pollWatch = useCallback(
    async (id: number) => {
      const result = await watchesApi.pollWatch(id)
      await loadAll()
      return result.newJobsCount
    },
    [loadAll],
  )

  const pollAllWatches = useCallback(async () => {
    for (const watch of watches) {
      if (watch.enabled) {
        await watchesApi.pollWatch(watch.id)
      }
    }
    await loadAll()
  }, [watches, loadAll])

  const value = useMemo(
    () => ({
      jobs,
      watches,
      loading,
      error,
      refreshJobs,
      refreshWatches,
      addWatch,
      removeWatch,
      pollWatch,
      pollAllWatches,
    }),
    [
      jobs,
      watches,
      loading,
      error,
      refreshJobs,
      refreshWatches,
      addWatch,
      removeWatch,
      pollWatch,
      pollAllWatches,
    ],
  )

  return <AppDataContext.Provider value={value}>{children}</AppDataContext.Provider>
}

export function useAppData() {
  const ctx = useContext(AppDataContext)
  if (!ctx) {
    throw new Error('useAppData must be used within AppDataProvider')
  }
  return ctx
}
