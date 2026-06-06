import { useEffect, useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { PanelHeader } from '../components/PanelHeader'
import { ResumePanel } from '../components/ResumePanel'
import { ResumeViewer } from '../components/ResumeViewer'
import { latestTailoredForJob, type ResumeViewerTarget } from '../lib/resumeViewer'
import type { Job } from '../types'

const STEPS: Array<{ title: string; detail: string }> = [
  { title: 'Parse base resume', detail: 'Read skills, projects, and experience.' },
  { title: 'Extract job signals', detail: 'Identify role requirements from the selected job.' },
  { title: 'Rewrite bullets', detail: 'Prioritize relevant, proven experience.' },
  { title: 'Render PDF', detail: 'Export an ATS-friendly LaTeX resume.' },
] as const

function statusClassName(status: string) {
  return `tailor-status tailor-status-${status.toLowerCase().replaceAll(/[^a-z0-9]+/g, '-')}`
}

export function ResumesPage({ selectedJob }: { selectedJob: Job }) {
  const { baseResume, tailoredResumes, generatingJobId, generateResumeForJob } = useAppData()
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [viewerTarget, setViewerTarget] = useState<ResumeViewerTarget | null>(null)

  const jobId = Number(selectedJob.id)
  const isValidJob = Number.isFinite(jobId) && selectedJob.id !== 'empty'
  const existing = isValidJob ? latestTailoredForJob(tailoredResumes, jobId) : undefined
  const isGenerating = generatingJobId === jobId

  useEffect(() => {
    if (existing) {
      setViewerTarget({ kind: 'tailored', id: existing.id })
    } else if (baseResume) {
      setViewerTarget({ kind: 'base' })
    } else {
      setViewerTarget(null)
    }
  }, [jobId, existing?.id, baseResume?.id])

  const stepStates = [
    baseResume ? 'Complete' : 'Add base resume',
    isValidJob ? 'Complete' : 'Select a job',
    isGenerating ? 'Running' : existing ? 'Complete' : baseResume && isValidJob ? 'Ready' : 'Waiting',
    existing
      ? existing.outputStatus === 'PDF_READY'
        ? 'Complete'
        : existing.outputStatus === 'LATEX_ONLY'
          ? 'LaTeX ready'
          : 'Failed'
      : 'Waiting',
  ]

  async function handleGenerate() {
    if (!isValidJob) {
      setError('Select a job from Jobs or Dashboard first.')
      return
    }
    if (!baseResume) {
      setError('Save a base resume in the panel on the right.')
      return
    }
    setError(null)
    setMessage(null)
    try {
      const result = await generateResumeForJob(jobId)
      setViewerTarget({ kind: 'tailored', id: result.id })
      setMessage(`Tailored resume generated for ${selectedJob.role}.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Generation failed')
    }
  }

  return (
    <section className="content-grid resume-layout layout-fill">
      <article className="panel workflow-panel panel-scroll-column">
        <div className="panel-scroll-header">
          <PanelHeader
            title="Tailoring workflow"
            action={isGenerating ? 'Generating...' : 'Run for selected job'}
            onAction={() => void handleGenerate()}
          />
          {error && <p className="inline-message inline-message-error">{error}</p>}
          {message && <p className="inline-message">{message}</p>}
        </div>
        <div className="panel-scroll-body">
          <div className="tailor-steps">
            {STEPS.map((step, index) => (
              <div className="tailor-step" key={step.title}>
                <span className="tailor-step-number">{index + 1}</span>
                <div>
                  <div className="tailor-step-heading">
                    <strong>{step.title}</strong>
                    <small className={statusClassName(stepStates[index])}>{stepStates[index]}</small>
                  </div>
                  <p>{step.detail}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="workflow-context">
            <div className="workflow-job-card">
              <span className="eyebrow">Selected job</span>
              <h3>{selectedJob.role}</h3>
              <p>
                {selectedJob.company} - {selectedJob.location}
              </p>
            </div>
            <div className="workflow-guard-card">
              <span className="eyebrow">No fabrication guard</span>
              <h3>Grounded edits only</h3>
              <p>Generated bullets stay anchored to the base resume content.</p>
            </div>
          </div>
        </div>
      </article>

      <ResumeViewer target={viewerTarget} />

      <ResumePanel full viewerTarget={viewerTarget} onViewTarget={setViewerTarget} />
    </section>
  )
}
