import type { ApplicationStatus } from '../api/types'
import type { JobStatus } from '../types'

export function toApiApplicationStatus(status: JobStatus): ApplicationStatus {
  switch (status) {
    case 'Viewed':
      return 'VIEWED'
    case 'Applied':
      return 'APPLIED'
    case 'Rejected':
      return 'REJECTED'
    default:
      return 'NEW'
  }
}

export function fromApiApplicationStatus(status: ApplicationStatus | undefined): JobStatus {
  switch (status) {
    case 'VIEWED':
      return 'Viewed'
    case 'APPLIED':
      return 'Applied'
    case 'REJECTED':
      return 'Rejected'
    default:
      return 'New'
  }
}

export const TRACKER_STATUSES: JobStatus[] = ['New', 'Viewed', 'Applied', 'Rejected']
