import { apiFetch } from './client'
import type { CompanyWatch, CreateWatchRequest } from './types'

export async function listWatches() {
  return apiFetch<CompanyWatch[]>('/api/watches')
}

export async function createWatch(body: CreateWatchRequest) {
  return apiFetch<CompanyWatch>('/api/watches', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function deleteWatch(id: number) {
  return apiFetch<void>(`/api/watches/${id}`, { method: 'DELETE' })
}

export async function pollWatch(id: number) {
  return apiFetch<{ watchId: number; newJobsCount: number }>(`/api/watches/${id}/poll`, {
    method: 'POST',
  })
}
