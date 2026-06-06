import { PanelHeader } from './PanelHeader'
import type { Job } from '../types'

type ActivityPanelProps = {
  jobs: Job[]
}

export function ActivityPanel({ jobs }: ActivityPanelProps) {
  const activity = jobs.slice(0, 8).map((job) => `${job.company}: ${job.role} discovered.`)

  return (
    <article className="panel activity-panel panel-scroll-column">
      <div className="panel-scroll-header">
        <PanelHeader title="Recent activity" action="Live feed" />
      </div>
      <div className="activity-list panel-scroll-body">
        {activity.length > 0 ? (
          activity.map((item) => <p key={item}>{item}</p>)
        ) : (
          <p className="empty-state">Activity will appear after jobs are discovered.</p>
        )}
      </div>
    </article>
  )
}
