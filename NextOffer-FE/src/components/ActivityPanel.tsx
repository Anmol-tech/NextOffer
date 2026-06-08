import { PanelHeader } from './PanelHeader'
import type { Job } from '../types'

type ActivityPanelProps = {
  jobs: Job[]
}

export function ActivityPanel({ jobs }: ActivityPanelProps) {
  const activity = jobs.slice(0, 10).map((job) => ({
    id: job.id,
    text: `${job.company}: ${job.role}`,
    meta: `${job.status} · ${job.firstSeen}`,
  }))

  return (
    <article className="panel activity-panel panel-scroll-column">
      <div className="panel-scroll-header">
        <PanelHeader title="Recent activity" />
      </div>
      <div className="activity-list panel-scroll-body">
        {activity.length > 0 ? (
          activity.map((item) => (
            <div className="activity-row" key={item.id}>
              <p>{item.text}</p>
              <span>{item.meta}</span>
            </div>
          ))
        ) : (
          <p className="empty-state">Activity will appear after jobs are discovered.</p>
        )}
      </div>
    </article>
  )
}
