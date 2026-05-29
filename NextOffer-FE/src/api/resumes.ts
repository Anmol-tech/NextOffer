import { ApiError, apiFetch, getStoredToken } from './client'
import type { BaseResume, TailoredResumeDetail, TailoredResumeSummary } from './types'

export async function getBaseResume() {
  return apiFetch<BaseResume>('/api/resumes/base')
}

export async function saveBaseResume(rawText: string) {
  return apiFetch<BaseResume>('/api/resumes/base', {
    method: 'PUT',
    body: JSON.stringify({ rawText }),
  })
}

export async function listTailoredResumes() {
  return apiFetch<TailoredResumeSummary[]>('/api/resumes/tailored')
}

export async function getTailoredResume(id: number) {
  return apiFetch<TailoredResumeDetail>(`/api/resumes/tailored/${id}`)
}

export async function generateResumeForJob(jobId: number) {
  return apiFetch<TailoredResumeDetail>(`/api/jobs/${jobId}/resumes/generate`, {
    method: 'POST',
  })
}

export async function compileTailoredResumePdf(id: number) {
  return apiFetch<TailoredResumeDetail>(`/api/resumes/tailored/${id}/compile-pdf`, {
    method: 'POST',
  })
}

export async function downloadTailoredResume(id: number, format: 'pdf' | 'latex' = 'pdf') {
  const token = getStoredToken()
  const headers = new Headers()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`/api/resumes/tailored/${id}/download?format=${format}`, { headers })

  if (!response.ok) {
    const text = await response.text()
    let message = `Download failed (${response.status})`
    try {
      const problem = JSON.parse(text) as { detail?: string; title?: string }
      message = problem.detail ?? problem.title ?? message
    } catch {
      // ignore parse errors
    }
    throw new ApiError(response.status, message, null)
  }

  const blob = await response.blob()
  const disposition = response.headers.get('Content-Disposition') ?? ''
  const filenameMatch = disposition.match(/filename="?([^";]+)"?/)
  const filename = filenameMatch?.[1] ?? `resume-${id}.${format === 'pdf' ? 'pdf' : 'tex'}`

  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

export function isBaseResumeMissing(error: unknown) {
  return error instanceof ApiError && error.status === 404
}
