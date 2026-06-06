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
import * as resumesApi from '../api/resumes'
import * as watchesApi from '../api/watches'
import { mapJobPosting } from '../lib/mappers'
import { toApiApplicationStatus } from '../lib/trackerStatus'
import type { BaseResume, CompanyWatch, TailoredResumeDetail, TailoredResumeSummary } from '../api/types'
import type { Job, JobStatus } from '../types'
import { useAuth } from './AuthContext'

type AppDataContextValue = {
  jobs: Job[]
  watches: CompanyWatch[]
  baseResume: BaseResume | null
  tailoredResumes: TailoredResumeSummary[]
  resumeLoading: boolean
  resumeError: string | null
  generatingJobId: number | null
  loading: boolean
  error: string | null
  refreshJobs: () => Promise<void>
  refreshWatches: () => Promise<void>
  refreshResumes: () => Promise<void>
  saveBaseResume: (rawText: string) => Promise<void>
  generateResumeForJob: (jobId: number) => Promise<TailoredResumeSummary>
  downloadResume: (id: number, format?: 'pdf' | 'latex') => Promise<void>
  compileResumePdf: (id: number) => Promise<TailoredResumeDetail>
  addWatch: (
    companyName: string,
    careerPageUrl: string,
    filters?: { locationFilter?: string; keywordFilter?: string; departmentFilter?: string },
    atsType?: CompanyWatch['atsType'],
  ) => Promise<void>
  updateWatchFilters: (
    id: number,
    filters: { locationFilter?: string; keywordFilter?: string; departmentFilter?: string },
  ) => Promise<void>
  removeWatch: (id: number) => Promise<void>
  pollWatch: (id: number) => Promise<number>
  pollAllWatches: () => Promise<void>
  updateJobApplicationStatus: (jobId: number, status: JobStatus) => Promise<void>
}

const AppDataContext = createContext<AppDataContextValue | null>(null)

export function AppDataProvider({ children }: { children: ReactNode }) {
  const { token } = useAuth()
  const [jobs, setJobs] = useState<Job[]>([])
  const [watches, setWatches] = useState<CompanyWatch[]>([])
  const [baseResume, setBaseResume] = useState<BaseResume | null>(null)
  const [tailoredResumes, setTailoredResumes] = useState<TailoredResumeSummary[]>([])
  const [resumeLoading, setResumeLoading] = useState(false)
  const [resumeError, setResumeError] = useState<string | null>(null)
  const [generatingJobId, setGeneratingJobId] = useState<number | null>(null)
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

  const refreshResumes = useCallback(async () => {
    setResumeLoading(true)
    setResumeError(null)
    try {
      const [baseResult, tailored] = await Promise.all([
        resumesApi.getBaseResume().catch((err) => {
          if (resumesApi.isBaseResumeMissing(err)) {
            return null
          }
          throw err
        }),
        resumesApi.listTailoredResumes(),
      ])
      setBaseResume(baseResult)
      setTailoredResumes(tailored)
    } catch (err) {
      setResumeError(err instanceof Error ? err.message : 'Failed to load resumes')
    } finally {
      setResumeLoading(false)
    }
  }, [])

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      await Promise.all([refreshJobs(), refreshWatches(), refreshResumes()])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data')
    } finally {
      setLoading(false)
    }
  }, [refreshJobs, refreshWatches, refreshResumes])

  useEffect(() => {
    if (token) {
      void loadAll()
    } else {
      setJobs([])
      setWatches([])
      setBaseResume(null)
      setTailoredResumes([])
      setError(null)
      setResumeError(null)
    }
  }, [token, loadAll])

  const saveBaseResume = useCallback(
    async (rawText: string) => {
      setResumeError(null)
      const saved = await resumesApi.saveBaseResume(rawText)
      setBaseResume(saved)
    },
    [],
  )

  const generateResumeForJob = useCallback(async (jobId: number) => {
    setResumeError(null)
    setGeneratingJobId(jobId)
    try {
      const detail = await resumesApi.generateResumeForJob(jobId)
      await refreshResumes()
      return {
        id: detail.id,
        jobPostingId: detail.jobPostingId,
        jobTitle: detail.jobTitle,
        companyName: detail.companyName,
        outputStatus: detail.outputStatus,
        summaryPreview: detail.content.summary,
        createdAt: detail.createdAt,
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Generation failed'
      setResumeError(message)
      throw err
    } finally {
      setGeneratingJobId(null)
    }
  }, [refreshResumes])

  const downloadResume = useCallback(async (id: number, format: 'pdf' | 'latex' = 'pdf') => {
    setResumeError(null)
    await resumesApi.downloadTailoredResume(id, format)
  }, [])

  const compileResumePdf = useCallback(async (id: number) => {
    setResumeError(null)
    const detail = await resumesApi.compileTailoredResumePdf(id)
    await refreshResumes()
    return detail
  }, [refreshResumes])

  const addWatch = useCallback(
    async (
      companyName: string,
      careerPageUrl: string,
      filters?: { locationFilter?: string; keywordFilter?: string; departmentFilter?: string },
      atsType: CompanyWatch['atsType'] = 'GREENHOUSE',
    ) => {
      await watchesApi.createWatch({
        companyName,
        careerPageUrl,
        atsType,
        enabled: true,
        locationFilter: filters?.locationFilter?.trim() || undefined,
        keywordFilter: filters?.keywordFilter?.trim() || undefined,
        departmentFilter: filters?.departmentFilter?.trim() || undefined,
      })
      await loadAll()
    },
    [loadAll],
  )

  const updateWatchFilters = useCallback(
    async (
      id: number,
      filters: { locationFilter?: string; keywordFilter?: string; departmentFilter?: string },
    ) => {
      await watchesApi.updateWatch(id, {
        locationFilter: filters.locationFilter?.trim() ?? '',
        keywordFilter: filters.keywordFilter?.trim() ?? '',
        departmentFilter: filters.departmentFilter?.trim() ?? '',
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

  const updateJobApplicationStatus = useCallback(async (jobId: number, status: JobStatus) => {
    const updated = await jobsApi.updateApplicationStatus(jobId, toApiApplicationStatus(status))
    setJobs((current) =>
      current.map((job) => (job.id === String(updated.id) ? mapJobPosting(updated) : job)),
    )
  }, [])

  const value = useMemo(
    () => ({
      jobs,
      watches,
      baseResume,
      tailoredResumes,
      resumeLoading,
      resumeError,
      generatingJobId,
      loading,
      error,
      refreshJobs,
      refreshWatches,
      refreshResumes,
      saveBaseResume,
      generateResumeForJob,
      downloadResume,
      compileResumePdf,
      addWatch,
      updateWatchFilters,
      removeWatch,
      pollWatch,
      pollAllWatches,
      updateJobApplicationStatus,
    }),
    [
      jobs,
      watches,
      baseResume,
      tailoredResumes,
      resumeLoading,
      resumeError,
      generatingJobId,
      loading,
      error,
      refreshJobs,
      refreshWatches,
      refreshResumes,
      saveBaseResume,
      generateResumeForJob,
      downloadResume,
      compileResumePdf,
      addWatch,
      updateWatchFilters,
      removeWatch,
      pollWatch,
      pollAllWatches,
      updateJobApplicationStatus,
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
