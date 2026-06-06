import { useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { latestTailoredForJob, type ResumeViewerTarget } from '../lib/resumeViewer'
import { TRACKER_STATUSES } from '../lib/trackerStatus'
import type { Job, JobStatus } from '../types'
import { PanelHeader } from './PanelHeader'
import { ResumeViewerModal } from './ResumeViewerModal'

export function JobDetailPanel({
  job,
  onViewResume,
}: {
  job: Job
  onViewResume?: (target: ResumeViewerTarget) => void
}) {
  const { baseResume, tailoredResumes, generatingJobId, generateResumeForJob, downloadResume, updateJobApplicationStatus } =
    useAppData()
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [statusUpdating, setStatusUpdating] = useState(false)
  const [localViewerTarget, setLocalViewerTarget] = useState<ResumeViewerTarget | null>(null)

  const openViewer = onViewResume ?? setLocalViewerTarget

  const jobId = Number(job.id)
  const isValidJob = Number.isFinite(jobId) && job.id !== 'empty'
  const existing = isValidJob ? latestTailoredForJob(tailoredResumes, jobId) : undefined
  const isGenerating = generatingJobId === jobId

  function openApplyLink() {
    if (job.applyUrl && job.applyUrl !== '#') {
      window.open(job.applyUrl, '_blank', 'noopener,noreferrer')
    }
  }

  async function handleGenerate() {
    if (!isValidJob) {
      return
    }
    if (!baseResume) {
      setError('Save a base resume first (Resumes tab).')
      return
    }
    setError(null)
    setMessage(null)
    try {
      const result = await generateResumeForJob(jobId)
      setMessage(`Tailored resume created for ${result.jobTitle}.`)
      openViewer({ kind: 'tailored', id: result.id })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Generation failed')
    }
  }

  async function handleStatusChange(status: JobStatus) {
    if (!isValidJob || job.status === status) {
      return
    }
    setStatusUpdating(true)
    setError(null)
    try {
      await updateJobApplicationStatus(jobId, status)
      setMessage(`Marked as ${status.toLowerCase()}.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not update tracker status')
    } finally {
      setStatusUpdating(false)
    }
  }

  return (
    <>
      <article className="panel detail-panel panel-scroll-column">
        <div className="detail-panel-top panel-scroll-header">
          <PanelHeader title="Job detail" action="Open link" onAction={openApplyLink} />
          <div className="selected-job">
            <div>
              <span className="company-chip">{job.company}</span>
              <h2>{job.role}</h2>
              <p>
                {job.location} - first seen {job.firstSeen} -{' '}
                <span className={`status status-${job.status.toLowerCase()}`}>{job.status}</span>
              </p>
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
        </div>

        <div className="detail-panel-scroll panel-scroll-body">
          <ul className="highlight-list">
            {job.highlights.map((highlight) => (
              <li key={highlight}>{highlight}</li>
            ))}
          </ul>

          {error && <p className="inline-message inline-message-error">{error}</p>}
          {message && <p className="inline-message">{message}</p>}
          {existing && (
            <p className="inline-message">
              Tailored version available ({existing.outputStatus.replace('_', ' ').toLowerCase()}).
            </p>
          )}

          {isValidJob && (
            <div className="tracker-quick-actions">
              {TRACKER_STATUSES.filter((status) => status !== job.status).map((status) => (
                <button
                  className={`tracker-action-chip tracker-action-chip-${status.toLowerCase()}`}
                  disabled={statusUpdating}
                  key={status}
                  onClick={() => void handleStatusChange(status)}
                  type="button"
                >
                  {status}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="detail-panel-footer panel-scroll-header">
          <div className="detail-actions">
            <button
              className="primary-button"
              disabled={!isValidJob || isGenerating}
              onClick={() => void handleGenerate()}
              type="button"
            >
              {isGenerating ? 'Generating…' : existing ? 'Regenerate tailored PDF' : 'Generate tailored PDF'}
            </button>
            {existing && (
              <button
                className="ghost-button"
                onClick={() => openViewer({ kind: 'tailored', id: existing.id })}
                type="button"
              >
                View resume
              </button>
            )}
            {existing && (existing.outputStatus === 'PDF_READY' || existing.outputStatus === 'LATEX_ONLY') && (
              <>
                {existing.outputStatus === 'PDF_READY' && (
                  <button
                    className="ghost-button"
                    onClick={() => void downloadResume(existing.id, 'pdf')}
                    type="button"
                  >
                    Download PDF
                  </button>
                )}
                <button
                  className="ghost-button"
                  onClick={() => void downloadResume(existing.id, 'latex')}
                  type="button"
                >
                  Download LaTeX
                </button>
              </>
            )}
            <button className="ghost-button" onClick={openApplyLink} type="button">
              Open apply link
            </button>
          </div>
        </div>
      </article>

      {!onViewResume && (
        <ResumeViewerModal target={localViewerTarget} onClose={() => setLocalViewerTarget(null)} />
      )}
    </>
  )
}
