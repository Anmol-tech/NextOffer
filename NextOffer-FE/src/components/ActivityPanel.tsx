import { activity } from '../data/mockData'
import { PanelHeader } from './PanelHeader'

export function ActivityPanel() {
  return (
    <article className="panel activity-panel">
      <PanelHeader title="Recent activity" action="Timeline" />
      <div className="activity-list">
        {activity.map((item) => (
          <p key={item}>{item}</p>
        ))}
      </div>
    </article>
  )
}
