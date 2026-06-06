import { apiFetch } from './client'
import type { ApplicationStatus, JobPosting } from './types'

export async function listJobs() {
  return apiFetch<JobPosting[]>('/api/jobs')
}

export async function getJob(id: number) {
  return apiFetch<JobPosting>(`/api/jobs/${id}`)
}

export async function updateApplicationStatus(id: number, status: ApplicationStatus) {
  return apiFetch<JobPosting>(`/api/jobs/${id}/application-status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}
