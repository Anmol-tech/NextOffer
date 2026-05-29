import { jobs } from '../data/mockData'

type JobListProps = {
  selectedJobId: string
  onSelectJob: (id: string) => void
  compact?: boolean
}

export function JobList({ selectedJobId, onSelectJob, compact = false }: JobListProps) {
  const visibleJobs = compact ? jobs.slice(0, 3) : jobs

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
            <strong>{job.match}%</strong>
          </div>
        </button>
      ))}
    </div>
  )
}
