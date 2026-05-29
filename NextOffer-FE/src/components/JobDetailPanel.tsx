import type { Job } from '../types'
import { PanelHeader } from './PanelHeader'

export function JobDetailPanel({ job }: { job: Job }) {
  return (
    <article className="panel detail-panel">
      <PanelHeader title="Job detail" action="Open link" />
      <div className="selected-job">
        <div>
          <span className="company-chip">{job.company}</span>
          <h2>{job.role}</h2>
          <p>{job.location} - first seen {job.firstSeen} - {job.salary}</p>
        </div>
        <div className="match-ring">
          <strong>{job.match}%</strong>
          <span>match</span>
        </div>
      </div>
      <div className="tag-list">
        {job.stack.map((skill) => (
          <span key={skill}>{skill}</span>
        ))}
      </div>
      <ul className="highlight-list">
        {job.highlights.map((highlight) => (
          <li key={highlight}>{highlight}</li>
        ))}
      </ul>
      <div className="detail-actions">
        <button className="primary-button" type="button">Generate tailored PDF</button>
        <button className="ghost-button" type="button">Save job</button>
      </div>
    </article>
  )
}
