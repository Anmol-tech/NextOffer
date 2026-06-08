import type { Job } from '../types'

type JobListProps = {
  jobs: Job[]
  selectedJobId: string
  onSelectJob: (id: string) => void
  limit?: number
}

export function JobList({ jobs, selectedJobId, onSelectJob, limit }: JobListProps) {
  const visibleJobs = limit ? jobs.slice(0, limit) : jobs

  if (visibleJobs.length === 0) {
    return <p className="empty-state">No jobs yet. Add a company watch and poll for openings.</p>
  }

  return (
    <div className="job-list">
      {visibleJobs.map((job) => (
        <button
          className={`job-row ${selectedJobId === job.id ? 'selected' : ''}`}
          key={job.id}
          onClick={() => onSelectJob(job.id)}
          type="button"
        >
          <div>
            <strong>{job.role}</strong>
            <span>{job.company} - {job.location}</span>
          </div>
          <div className="job-meta">
            <span className={`status status-${job.status.toLowerCase()}`}>{job.status}</span>
            {job.match > 0 && <strong>{job.match}%</strong>}
          </div>
        </button>
      ))}
    </div>
  )
}
