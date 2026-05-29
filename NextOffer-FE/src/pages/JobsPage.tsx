import { useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { JobDetailPanel } from '../components/JobDetailPanel'
import { JobList } from '../components/JobList'
import { PanelHeader } from '../components/PanelHeader'
import type { Job } from '../types'

type JobsPageProps = {
  jobs: Job[]
  selectedJob: Job
  selectedJobId: string
  onSelectJob: (id: string) => void
}

export function JobsPage({ jobs, selectedJob, selectedJobId, onSelectJob }: JobsPageProps) {
  const { pollAllWatches, refreshJobs } = useAppData()
  const [refreshing, setRefreshing] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  async function handleRefresh() {
    setRefreshing(true)
    setMessage(null)
    try {
      if (pollAllWatches) {
        await pollAllWatches()
      } else {
        await refreshJobs()
      }
      setMessage('Jobs refreshed from career page watches.')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Refresh failed')
    } finally {
      setRefreshing(false)
    }
  }

  return (
    <section className="content-grid jobs-layout">
      <article className="panel job-panel">
        <PanelHeader
          title="Career page matches"
          action={refreshing ? 'Refreshing…' : 'Refresh'}
          onAction={handleRefresh}
        />
        {message && <p className="inline-message">{message}</p>}
        <div className="filter-row">
          <span>{jobs.length} roles loaded</span>
          <span>Live from backend</span>
          <span>Newest first</span>
        </div>
        <JobList jobs={jobs} selectedJobId={selectedJobId} onSelectJob={onSelectJob} />
      </article>
      <JobDetailPanel job={selectedJob} />
    </section>
  )
}
