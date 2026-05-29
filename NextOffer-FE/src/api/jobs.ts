import { apiFetch } from './client'
import type { JobPosting } from './types'

export async function listJobs() {
  return apiFetch<JobPosting[]>('/api/jobs')
}

export async function getJob(id: number) {
  return apiFetch<JobPosting>(`/api/jobs/${id}`)
}
