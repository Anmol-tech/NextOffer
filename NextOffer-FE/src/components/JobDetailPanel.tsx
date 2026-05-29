import type { Job } from '../types'
import { PanelHeader } from './PanelHeader'

export function JobDetailPanel({ job }: { job: Job }) {
  function openApplyLink() {
    if (job.applyUrl && job.applyUrl !== '#') {
      window.open(job.applyUrl, '_blank', 'noopener,noreferrer')
    }
  }

  return (
    <article className="panel detail-panel">
      <PanelHeader title="Job detail" action="Open link" onAction={openApplyLink} />
      <div className="selected-job">
        <div>
          <span className="company-chip">{job.company}</span>
          <h2>{job.role}</h2>
          <p>{job.location} - first seen {job.firstSeen} - {job.salary}</p>
        </div>
        {job.match > 0 && (
          <div className="match-ring">
            <strong>{job.match}%</strong>
            <span>match</span>
          </div>
        )}
      </div>
      {job.stack.length > 0 && (
        <div className="tag-list">
          {job.stack.map((skill) => (
            <span key={skill}>{skill}</span>
          ))}
        </div>
      )}
      <ul className="highlight-list">
        {job.highlights.map((highlight) => (
          <li key={highlight}>{highlight}</li>
        ))}
      </ul>
      <div className="detail-actions">
        <button className="primary-button" disabled type="button">
          Generate tailored PDF
        </button>
        <button className="ghost-button" onClick={openApplyLink} type="button">
          Open apply link
        </button>
      </div>
    </article>
  )
}
