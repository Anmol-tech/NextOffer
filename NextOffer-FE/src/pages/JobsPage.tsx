import { JobDetailPanel } from '../components/JobDetailPanel'
import { JobList } from '../components/JobList'
import { PanelHeader } from '../components/PanelHeader'
import type { Job } from '../types'

type JobsPageProps = {
  selectedJob: Job
  selectedJobId: string
  onSelectJob: (id: string) => void
}

export function JobsPage({ selectedJob, selectedJobId, onSelectJob }: JobsPageProps) {
  return (
    <section className="content-grid jobs-layout">
      <article className="panel job-panel">
        <PanelHeader title="Career page matches" action="Refresh mock" />
        <div className="filter-row">
          <span>All SWE roles</span>
          <span>Internship + new grad</span>
          <span>Match high to low</span>
        </div>
        <JobList selectedJobId={selectedJobId} onSelectJob={onSelectJob} />
      </article>
      <JobDetailPanel job={selectedJob} />
    </section>
  )
}
