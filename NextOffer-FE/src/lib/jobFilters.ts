import type { Job } from '../types'

export type FilterOption = {
  label: string
  value: string
  count: number
}

export type JobListFilters = {
  locationFilter: string
  companyFilter: string
}

export function parseFilterTerms(filter: string): string[] {
  if (!filter.trim()) {
    return []
  }
  return filter
    .split(/[,;\n]+/)
    .map((term) => term.trim().toLowerCase())
    .filter(Boolean)
}

export function matchesTextFilter(filter: string, text: string | undefined): boolean {
  const terms = parseFilterTerms(filter)
  if (terms.length === 0) {
    return true
  }
  const haystack = (text ?? '').toLowerCase()
  return terms.some((term) => haystack.includes(term))
}

export function matchesLocationFilter(filter: string, location: string | undefined): boolean {
  return matchesTextFilter(filter, location)
}

export function matchesCompanyFilter(filter: string, company: string | undefined): boolean {
  return matchesTextFilter(filter, company)
}

export function filterJobs(jobs: Job[], filters: JobListFilters): Job[] {
  return jobs.filter(
    (job) =>
      matchesLocationFilter(filters.locationFilter, job.location) &&
      matchesCompanyFilter(filters.companyFilter, job.company),
  )
}

function titleCase(value: string): string {
  return value
    .split(/\s+/)
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

function normalizeLocationValue(raw: string): string | null {
  const trimmed = raw.trim()
  if (!trimmed || trimmed === '—' || trimmed === 'Unspecified') {
    return null
  }
  if (/remote/i.test(trimmed)) {
    return 'remote'
  }
  const city = trimmed.split(',')[0]?.trim() ?? trimmed
  if (!city || city.length > 32 || /travel[- ]required/i.test(city)) {
    return null
  }
  return city.toLowerCase()
}

function locationLabel(value: string): string {
  if (value === 'remote') {
    return 'Remote'
  }
  return titleCase(value)
}

/** Pull city / remote tokens from messy ATS location strings for quick-filter chips. */
export function extractLocationOptions(jobs: Job[]): FilterOption[] {
  const counts = new Map<string, number>()

  for (const job of jobs) {
    const location = job.location?.trim()
    if (!location) {
      continue
    }

    const tokens = new Set<string>()
    for (const segment of location.split(/[|,;/]+/)) {
      const value = normalizeLocationValue(segment)
      if (value) {
        tokens.add(value)
      }
    }

    const whole = normalizeLocationValue(location)
    if (whole) {
      tokens.add(whole)
    }

    for (const token of tokens) {
      counts.set(token, (counts.get(token) ?? 0) + 1)
    }
  }

  return toFilterOptions(counts, locationLabel)
}

/** Distinct companies from loaded jobs for quick-filter chips. */
export function extractCompanyOptions(jobs: Job[]): FilterOption[] {
  const counts = new Map<string, number>()

  for (const job of jobs) {
    const company = job.company?.trim()
    if (!company || company === 'No company') {
      continue
    }
    const value = company.toLowerCase()
    counts.set(value, (counts.get(value) ?? 0) + 1)
  }

  return toFilterOptions(counts, (value) => {
    const match = jobs.find((job) => job.company.toLowerCase() === value)
    return match?.company ?? titleCase(value)
  })
}

function toFilterOptions(
  counts: Map<string, number>,
  labelFor: (value: string) => string,
): FilterOption[] {
  return [...counts.entries()]
    .map(([value, count]) => ({ label: labelFor(value), value, count }))
    .sort((a, b) => b.count - a.count || a.label.localeCompare(b.label))
    .slice(0, 8)
}

export function hasActiveJobFilters(filters: JobListFilters): boolean {
  return Boolean(filters.locationFilter.trim() || filters.companyFilter.trim())
}

export function formatActiveJobFilters(filters: JobListFilters): string {
  const parts: string[] = []
  if (filters.locationFilter.trim()) {
    parts.push(`location "${filters.locationFilter.trim()}"`)
  }
  if (filters.companyFilter.trim()) {
    parts.push(`company "${filters.companyFilter.trim()}"`)
  }
  return parts.join(' · ')
}
