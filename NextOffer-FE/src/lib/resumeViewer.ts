import { useEffect, useRef, useState } from 'react'
import * as resumesApi from '../api/resumes'
import type { BaseResume, TailoredResumeDetail } from '../api/types'

export type ResumeViewerTarget =
  | { kind: 'base' }
  | { kind: 'tailored'; id: number }

export function latestTailoredForJob(
  tailoredResumes: import('../api/types').TailoredResumeSummary[],
  jobPostingId: number,
) {
  return tailoredResumes
    .filter((resume) => resume.jobPostingId === jobPostingId)
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())[0]
}

export async function getResumePdfPreviewUrl(id: number) {
  const token = localStorage.getItem('nextoffer_token')
  const headers = new Headers()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`/api/resumes/tailored/${id}/download?format=pdf`, { headers })
  if (!response.ok) {
    return null
  }

  const blob = await response.blob()
  return URL.createObjectURL(blob)
}

export function useResumeViewerData(
  target: ResumeViewerTarget | null,
  baseResume: BaseResume | null,
  reloadToken = 0,
) {
  const [detail, setDetail] = useState<TailoredResumeDetail | null>(null)
  const [pdfUrl, setPdfUrl] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const pdfUrlRef = useRef<string | null>(null)

  useEffect(() => {
    let cancelled = false

    function clearPdfUrl() {
      if (pdfUrlRef.current) {
        URL.revokeObjectURL(pdfUrlRef.current)
        pdfUrlRef.current = null
      }
      setPdfUrl(null)
    }

    async function load() {
      setError(null)
      setDetail(null)
      clearPdfUrl()

      if (!target) {
        setLoading(false)
        return
      }

      if (target.kind === 'base') {
        if (!baseResume) {
          setError('No base resume saved yet.')
        }
        setLoading(false)
        return
      }

      setLoading(true)
      try {
        const tailored = await resumesApi.getTailoredResume(target.id)
        if (cancelled) {
          return
        }
        setDetail(tailored)

        if (tailored.pdfAvailable) {
          const url = await getResumePdfPreviewUrl(target.id)
          if (!cancelled && url) {
            pdfUrlRef.current = url
            setPdfUrl(url)
          }
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Failed to load resume')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void load()

    return () => {
      cancelled = true
      if (pdfUrlRef.current) {
        URL.revokeObjectURL(pdfUrlRef.current)
        pdfUrlRef.current = null
      }
    }
  }, [target, baseResume, reloadToken])

  return { detail, pdfUrl, loading, error }
}
