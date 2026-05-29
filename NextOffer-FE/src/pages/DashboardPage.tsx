import { ActivityPanel } from '../components/ActivityPanel'
import { JobDetailPanel } from '../components/JobDetailPanel'
import { JobList } from '../components/JobList'
import { Metric } from '../components/Metric'
import { PanelHeader } from '../components/PanelHeader'
import { ResumePanel } from '../components/ResumePanel'
import type { Job, View } from '../types'

type DashboardPageProps = {
  selectedJob: Job
  onSelectJob: (id: string) => void
  onNavigate: (view: View) => void
}

export function DashboardPage({ selectedJob, onSelectJob, onNavigate }: DashboardPageProps) {
  return (
    <>
      <section className="metric-grid" aria-label="Application summary">
        <Metric label="Open roles" value="24" detail="+6 from watched companies" />
        <Metric label="High matches" value="8" detail="80% and above" />
        <Metric label="Tailored resumes" value="12" detail="3 pending review" />
        <Metric label="Applications" value="5" detail="2 follow-ups due" />
      </section>

      <section className="content-grid">
        <article className="panel job-panel">
          <PanelHeader title="Recommended jobs" action="View all" onAction={() => onNavigate('jobs')} />
          <JobList selectedJobId={selectedJob.id} onSelectJob={onSelectJob} compact />
        </article>

        <JobDetailPanel job={selectedJob} />
        <ResumePanel />
        <ActivityPanel />
      </section>
    </>
  )
}
