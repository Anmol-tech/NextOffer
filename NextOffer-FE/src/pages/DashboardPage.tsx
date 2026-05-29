import { useState } from 'react'
import { ActivityPanel } from '../components/ActivityPanel'
import { JobDetailPanel } from '../components/JobDetailPanel'
import { JobList } from '../components/JobList'
import { Metric } from '../components/Metric'
import { PanelHeader } from '../components/PanelHeader'
import { ResumePanel } from '../components/ResumePanel'
import { ResumeViewerModal } from '../components/ResumeViewerModal'
import { useAppData } from '../context/AppDataContext'
import type { ResumeViewerTarget } from '../lib/resumeViewer'
import type { Job, View } from '../types'

type DashboardPageProps = {
  jobs: Job[]
  selectedJob: Job
  onSelectJob: (id: string) => void
  onNavigate: (view: View) => void
}

export function DashboardPage({ jobs, selectedJob, onSelectJob, onNavigate }: DashboardPageProps) {
  const { tailoredResumes } = useAppData()
  const [viewerTarget, setViewerTarget] = useState<ResumeViewerTarget | null>(null)
  const newJobs = jobs.filter((job) => job.status === 'New').length

  return (
    <>
      <section className="metric-grid" aria-label="Application summary">
        <Metric label="Discovered jobs" value={String(jobs.length)} detail="From watched companies" />
        <Metric label="New roles" value={String(newJobs)} detail="Not yet reviewed" />
        <Metric label="Tailored resumes" value={String(tailoredResumes.length)} detail="Job-specific versions" />
        <Metric label="Applications" value="0" detail="Tracker API coming next" />
      </section>

      <section className="content-grid">
        <article className="panel job-panel">
          <PanelHeader title="Recommended jobs" action="View all" onAction={() => onNavigate('jobs')} />
          <JobList jobs={jobs} selectedJobId={selectedJob.id} onSelectJob={onSelectJob} compact />
        </article>

        <JobDetailPanel job={selectedJob} onViewResume={setViewerTarget} />
        <ResumePanel onViewTarget={setViewerTarget} />
        <ActivityPanel jobs={jobs} />
      </section>

      <ResumeViewerModal target={viewerTarget} onClose={() => setViewerTarget(null)} />
    </>
  )
}
