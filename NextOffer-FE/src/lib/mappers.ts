import type { JobPosting as ApiJobPosting } from '../api/types'
import type { Job } from '../types'

export function mapJobPosting(dto: ApiJobPosting): Job {
  const snippet = dto.description
    ? dto.description.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 160)
    : 'No description available yet.'

  return {
    id: String(dto.id),
    company: dto.companyName,
    role: dto.title,
    location: dto.location ?? 'Unspecified',
    match: 0,
    status: 'New',
    firstSeen: formatFirstSeen(dto.firstSeenAt),
    applyUrl: dto.applyUrl,
    salary: '—',
    stack: [],
    highlights: [snippet],
  }
}

function formatFirstSeen(iso: string): string {
  const date = new Date(iso)
  const diffMs = Date.now() - date.getTime()
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (days <= 0) return 'Today'
  if (days === 1) return 'Yesterday'
  if (days < 7) return `${days} days ago`
  return date.toLocaleDateString()
}
