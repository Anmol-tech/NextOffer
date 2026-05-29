import { PanelHeader } from '../components/PanelHeader'
import { ResumePanel } from '../components/ResumePanel'
import type { Job } from '../types'

export function ResumesPage({ selectedJob }: { selectedJob: Job }) {
  return (
    <section className="content-grid resume-layout">
      <article className="panel">
        <PanelHeader title="Tailoring workflow" action="Mock run" />
        <div className="tailor-steps">
          {['Parse base resume', 'Extract job signals', 'Rewrite relevant bullets', 'Render LaTeX PDF'].map(
            (step, index) => (
              <div className="tailor-step" key={step}>
                <span>{index + 1}</span>
                <strong>{step}</strong>
                <p>{index < 2 ? 'Complete' : index === 2 ? 'Reviewing' : 'Waiting'}</p>
              </div>
            ),
          )}
        </div>
        <div className="comparison-grid">
          <div>
            <span className="eyebrow">Selected job</span>
            <h2>{selectedJob.role}</h2>
            <p>{selectedJob.company} - {selectedJob.location}</p>
          </div>
          <div>
            <span className="eyebrow">No fabrication guard</span>
            <h2>Only reorder and rewrite proven experience</h2>
            <p>Generated bullets stay anchored to the base resume content.</p>
          </div>
        </div>
      </article>
      <ResumePanel full />
    </section>
  )
}
