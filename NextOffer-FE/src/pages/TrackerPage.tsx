import { useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { TRACKER_STATUSES } from '../lib/trackerStatus'
import type { Job, JobStatus } from '../types'

type TrackerPageProps = {
  jobs: Job[]
}

const COLUMN_META: Record<JobStatus, { hint: string }> = {
  New: { hint: 'Roles you have not opened yet' },
  Viewed: { hint: 'Roles you reviewed but have not applied to' },
  Applied: { hint: 'Applications you submitted' },
  Rejected: { hint: 'Roles you are no longer pursuing' },
}

export function TrackerPage({ jobs }: TrackerPageProps) {
  const { updateJobApplicationStatus } = useAppData()
  const [updatingId, setUpdatingId] = useState<string | null>(null)
  const [savedId, setSavedId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleStatusChange(job: Job, status: JobStatus) {
    if (job.status === status) {
      return
    }
    setUpdatingId(job.id)
    setError(null)
    setSavedId(null)
    try {
      await updateJobApplicationStatus(Number(job.id), status)
      setSavedId(job.id)
      window.setTimeout(() => {
        setSavedId((current) => (current === job.id ? null : current))
      }, 1800)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not update status')
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <section className="tracker-page layout-fill">
      {error && <p className="form-error tracker-page-error">{error}</p>}
      <section className="tracker-board">
        {TRACKER_STATUSES.map((status) => {
          const statusJobs = jobs.filter((job) => job.status === status)

          return (
            <article className={`tracker-column tracker-column-${status.toLowerCase()}`} key={status}>
              <header className="tracker-column-header">
                <div>
                  <h2>{status}</h2>
                  <p>{COLUMN_META[status].hint}</p>
                </div>
                <span className="tracker-column-count">{statusJobs.length}</span>
              </header>

              <div className="tracker-list">
                {statusJobs.map((job) => {
                  const isUpdating = updatingId === job.id
                  const nextStatuses = TRACKER_STATUSES.filter((option) => option !== job.status)

                  return (
                    <div className={`tracker-card${isUpdating ? ' tracker-card-busy' : ''}`} key={job.id}>
                      <div className="tracker-card-body">
                        <strong title={job.role}>{job.role}</strong>
                        <span>
                          {job.company} · {job.location}
                        </span>
                      </div>

                      <div className="tracker-card-footer">
                        <span className="tracker-card-label">
                          {savedId === job.id ? 'Saved' : 'Move to'}
                        </span>
                        <div className="tracker-action-row">
                          {nextStatuses.map((option) => (
                            <button
                              className={`tracker-action-chip tracker-action-chip-${option.toLowerCase()}`}
                              disabled={isUpdating}
                              key={option}
                              onClick={() => void handleStatusChange(job, option)}
                              type="button"
                            >
                              {option}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>
                  )
                })}

                {statusJobs.length === 0 && (
                  <p className="tracker-empty">No applications here yet.</p>
                )}
              </div>
            </article>
          )
        })}
      </section>
    </section>
  )
}
