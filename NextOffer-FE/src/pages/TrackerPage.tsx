import type { Job } from '../types'
import { PanelHeader } from '../components/PanelHeader'
import type { JobStatus } from '../types'

const statuses: JobStatus[] = ['New', 'Viewed', 'Applied', 'Rejected']

type TrackerPageProps = {
  jobs: Job[]
}

export function TrackerPage({ jobs }: TrackerPageProps) {
  return (
    <section className="tracker-board">
      {statuses.map((status) => {
        const statusJobs = jobs.filter((job) => job.status === status)

        return (
          <article className="panel tracker-column" key={status}>
            <PanelHeader title={status} action={`${statusJobs.length}`} />
            <div className="tracker-list">
              {statusJobs.map((job) => (
                <div className="tracker-card" key={job.id}>
                  <strong>{job.role}</strong>
                  <span>{job.company}</span>
                  <small>{job.firstSeen}</small>
                </div>
              ))}
              {statusJobs.length === 0 && (
                <p className="empty-state">No applications here.</p>
              )}
            </div>
          </article>
        )
      })}
    </section>
  )
}
