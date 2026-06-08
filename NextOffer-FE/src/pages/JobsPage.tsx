import { useEffect, useMemo, useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { JobDetailPanel } from '../components/JobDetailPanel'
import { JobList } from '../components/JobList'
import { PanelHeader } from '../components/PanelHeader'
import {
  extractCompanyOptions,
  extractLocationOptions,
  filterJobs,
  formatActiveJobFilters,
  hasActiveJobFilters,
} from '../lib/jobFilters'
import type { Job } from '../types'

type JobsPageProps = {
  jobs: Job[]
  selectedJob: Job
  selectedJobId: string
  onSelectJob: (id: string) => void
}

type FilterChipProps = {
  active: boolean
  count?: number
  label: string
  onClick: () => void
  title?: string
}

function FilterChip({ active, count, label, onClick, title }: FilterChipProps) {
  return (
    <button
      className={`filter-chip${active ? ' active' : ''}`}
      onClick={onClick}
      title={title}
      type="button"
    >
      {label}
      {count !== undefined && <span className="filter-chip-count">{count}</span>}
    </button>
  )
}

export function JobsPage({ jobs, selectedJob, selectedJobId, onSelectJob }: JobsPageProps) {
  const { pollAllWatches, refreshJobs } = useAppData()
  const [refreshing, setRefreshing] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [locationFilter, setLocationFilter] = useState('')
  const [companyFilter, setCompanyFilter] = useState('')

  const locationOptions = useMemo(() => extractLocationOptions(jobs), [jobs])
  const companyOptions = useMemo(() => extractCompanyOptions(jobs), [jobs])
  const filters = useMemo(
    () => ({ locationFilter, companyFilter }),
    [locationFilter, companyFilter],
  )
  const filteredJobs = useMemo(() => filterJobs(jobs, filters), [jobs, filters])
  const activeFilterSummary = formatActiveJobFilters(filters)

  useEffect(() => {
    if (filteredJobs.length === 0) {
      return
    }
    if (!filteredJobs.some((job) => job.id === selectedJobId)) {
      onSelectJob(filteredJobs[0].id)
    }
  }, [filteredJobs, selectedJobId, onSelectJob])

  const detailJob =
    filteredJobs.find((job) => job.id === selectedJobId) ?? filteredJobs[0] ?? selectedJob

  function clearAllFilters() {
    setLocationFilter('')
    setCompanyFilter('')
  }

  async function handleRefresh() {
    setRefreshing(true)
    setMessage(null)
    try {
      if (pollAllWatches) {
        await pollAllWatches()
      } else {
        await refreshJobs()
      }
      setMessage('Jobs refreshed from career page watches.')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Refresh failed')
    } finally {
      setRefreshing(false)
    }
  }

  return (
    <section className="content-grid jobs-layout layout-fill">
      <article className="panel job-panel">
        <div className="job-panel-top">
          <PanelHeader
            title="Career page matches"
            action={refreshing ? 'Refreshing…' : 'Refresh'}
            onAction={handleRefresh}
          />
          {message && <p className="inline-message">{message}</p>}
          <div className="job-filter-bar">
            <div className="job-filter-section">
              <div className="job-filter-input-row">
                <span className="job-filter-label">Location</span>
                <input
                  aria-label="Filter jobs by location"
                  className="job-filter-input"
                  placeholder="City or Remote…"
                  value={locationFilter}
                  onChange={(event) => setLocationFilter(event.target.value)}
                />
              </div>
              {locationOptions.length > 0 && (
                <div className="job-filter-chips-scroll">
                  <FilterChip
                    active={locationFilter === ''}
                    label="All"
                    onClick={() => setLocationFilter('')}
                  />
                  {locationOptions.map((option) => (
                    <FilterChip
                      active={locationFilter.toLowerCase() === option.value}
                      count={option.count}
                      key={option.value}
                      label={option.label}
                      onClick={() => setLocationFilter(option.value)}
                      title={`${option.label} (${option.count} roles)`}
                    />
                  ))}
                </div>
              )}
            </div>

            <div className="job-filter-section">
              <div className="job-filter-input-row">
                <span className="job-filter-label">Company</span>
                <input
                  aria-label="Filter jobs by company"
                  className="job-filter-input"
                  placeholder="Stripe, Anthropic…"
                  value={companyFilter}
                  onChange={(event) => setCompanyFilter(event.target.value)}
                />
              </div>
              {companyOptions.length > 0 && (
                <div className="job-filter-chips-scroll">
                  <FilterChip
                    active={companyFilter === ''}
                    label="All"
                    onClick={() => setCompanyFilter('')}
                  />
                  {companyOptions.map((option) => (
                    <FilterChip
                      active={companyFilter.toLowerCase() === option.value}
                      count={option.count}
                      key={option.value}
                      label={option.label}
                      onClick={() => setCompanyFilter(option.value)}
                      title={`${option.label} (${option.count} roles)`}
                    />
                  ))}
                </div>
              )}
            </div>

            <div className="job-filter-footer">
              <p className="job-filter-meta">
                <strong>{filteredJobs.length}</strong> of {jobs.length} roles
                {activeFilterSummary ? ` · ${activeFilterSummary}` : ''}
              </p>
              {hasActiveJobFilters(filters) && (
                <button className="ghost-button job-filter-clear" onClick={clearAllFilters} type="button">
                  Clear filters
                </button>
              )}
            </div>
          </div>
        </div>
        <div className="job-panel-scroll">
          {filteredJobs.length === 0 ? (
            <p className="empty-state">
              No jobs match these filters. Try another location or company, or clear filters.
            </p>
          ) : (
            <JobList jobs={filteredJobs} selectedJobId={selectedJobId} onSelectJob={onSelectJob} />
          )}
        </div>
      </article>
      <JobDetailPanel job={detailJob} />
    </section>
  )
}
