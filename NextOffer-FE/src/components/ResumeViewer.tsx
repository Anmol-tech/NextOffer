import { useEffect, useMemo, useRef, useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { useAuth } from '../context/AuthContext'
import { formatLineForDisplay, parseLatexForPreview, parsePlainTextForPreview } from '../lib/latexPreview'
import { useResumeViewerData, type ResumeViewerTarget } from '../lib/resumeViewer'
import { PanelHeader } from './PanelHeader'

type ResumeViewerProps = {
  target: ResumeViewerTarget | null
  onClose?: () => void
}

type ViewerTab = 'preview' | 'source'

function formatCreated(iso: string) {
  return new Date(iso).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export function ResumeViewer({ target, onClose }: ResumeViewerProps) {
  const { user } = useAuth()
  const { baseResume, downloadResume, compileResumePdf } = useAppData()
  const [reloadToken, setReloadToken] = useState(0)
  const [compilingPdf, setCompilingPdf] = useState(false)
  const { detail, pdfUrl, loading, error } = useResumeViewerData(target, baseResume, reloadToken)
  const [tab, setTab] = useState<ViewerTab>('preview')
  const lastTargetKey = useRef<string | null>(null)

  const targetKey = target ? (target.kind === 'tailored' ? `tailored:${target.id}` : 'base') : null

  useEffect(() => {
    if (!targetKey) {
      return
    }
    lastTargetKey.current = targetKey
    setTab('preview')
  }, [targetKey])

  const title = useMemo(() => {
    if (!target) {
      return 'Resume preview'
    }
    if (target.kind === 'base') {
      return 'Base resume'
    }
    return detail?.jobTitle ?? 'Tailored resume'
  }, [target, detail])

  const subtitle = useMemo(() => {
    if (!target) {
      return 'Select a base or tailored version to preview.'
    }
    if (target.kind === 'base') {
      return baseResume
        ? `${baseResume.sourceFormat} source — updated ${formatCreated(baseResume.updatedAt)}`
        : 'Upload a base resume to get started.'
    }
    if (detail) {
      return `${detail.companyName} — ${formatCreated(detail.createdAt)}`
    }
    return 'Loading resume details…'
  }, [target, baseResume, detail])

  const basePreview = useMemo(() => {
    if (!baseResume) {
      return null
    }
    return baseResume.sourceFormat === 'LATEX'
      ? parseLatexForPreview(baseResume.rawText)
      : parsePlainTextForPreview(baseResume.rawText)
  }, [baseResume])

  const hasRenderedPdf = target?.kind === 'tailored' && Boolean(pdfUrl)

  return (
    <article className="panel resume-viewer-panel panel-scroll-column">
      <div className="panel-scroll-header">
        <PanelHeader title={title} action={onClose ? 'Close' : undefined} onAction={onClose} />
        <p className="resume-viewer-subtitle">{subtitle}</p>

        {target && (
          <div className="viewer-tabs" role="tablist" aria-label="Resume view">
            <button
              className={tab === 'preview' ? 'active' : ''}
              onClick={() => setTab('preview')}
              type="button"
            >
              Preview
            </button>
            <button
              className={tab === 'source' ? 'active' : ''}
              onClick={() => setTab('source')}
              type="button"
            >
              Source
            </button>
          </div>
        )}

        {error && <p className="inline-message inline-message-error">{error}</p>}
        {loading && <p className="inline-message">Loading resume…</p>}
      </div>

      <div className="panel-scroll-body resume-viewer-scroll">
        {!target && !loading && (
          <div className="resume-viewer-empty">
            <strong>No resume selected</strong>
            <p>View your base resume or pick a tailored version from the list.</p>
          </div>
        )}

        {target?.kind === 'tailored' && !detail && loading && (
          <p className="inline-message">Loading tailored resume…</p>
        )}

        {target?.kind === 'tailored' && !detail && !loading && !error && (
          <div className="resume-viewer-empty">
            <strong>Tailored resume not loaded</strong>
            <p>Select a version from the list or generate one for a job.</p>
          </div>
        )}

        {target?.kind === 'base' && baseResume && tab === 'preview' && basePreview && (
          <RenderedResumeDocument
            companyName="Base resume"
            email={user?.email ?? ''}
            fullName={user?.fullName ?? 'Your name'}
            jobTitle="Stored template"
            parsed={basePreview}
          />
        )}

        {target?.kind === 'base' && baseResume && tab === 'source' && (
          <pre className="resume-source-block">{baseResume.rawText}</pre>
        )}

        {target?.kind === 'tailored' && detail && tab === 'preview' && hasRenderedPdf && pdfUrl && (
          <iframe className="resume-pdf-frame" src={pdfUrl} title={`Resume PDF for ${detail.jobTitle}`} />
        )}

        {target?.kind === 'tailored' && detail && tab === 'preview' && !hasRenderedPdf && (
          <>
            {detail.outputStatus === 'LATEX_ONLY' && (
              <p className="inline-message">
                PDF not generated yet. Use Generate PDF to compile from LaTeX.
              </p>
            )}
            <RenderedResumeDocument
              companyName={detail.companyName}
              email={user?.email ?? ''}
              fullName={user?.fullName ?? 'Your name'}
              jobTitle={detail.jobTitle}
              parsed={{
                summary: detail.content.summary,
                skills: detail.content.skills,
                experienceBullets: detail.content.experienceBullets,
                sections: [],
              }}
            />
          </>
        )}

        {target?.kind === 'tailored' && detail && tab === 'source' && (
          <pre className="resume-source-block">
            {detail.latexContent ?? JSON.stringify(detail.content, null, 2)}
          </pre>
        )}
      </div>

      {target?.kind === 'tailored' && detail && (
        <div className="resume-viewer-actions panel-scroll-header">
          {!detail.pdfAvailable && detail.latexContent && (
            <button
              className="primary-button"
              disabled={compilingPdf}
              onClick={() => {
                setCompilingPdf(true)
                void compileResumePdf(detail.id)
                  .then(() => setReloadToken((value) => value + 1))
                  .finally(() => setCompilingPdf(false))
              }}
              type="button"
            >
              {compilingPdf ? 'Generating PDF…' : 'Generate PDF'}
            </button>
          )}
          {detail.pdfAvailable && (
            <button className="ghost-button" onClick={() => void downloadResume(detail.id, 'pdf')} type="button">
              Download PDF
            </button>
          )}
          <button className="ghost-button" onClick={() => void downloadResume(detail.id, 'latex')} type="button">
            Download LaTeX
          </button>
        </div>
      )}
    </article>
  )
}

function RenderedResumeDocument({
  fullName,
  email,
  jobTitle,
  companyName,
  parsed,
}: {
  fullName: string
  email: string
  jobTitle: string
  companyName: string
  parsed: {
    summary: string
    skills: string[]
    experienceBullets: string[]
    sections: { title: string; lines: string[] }[]
  }
}) {
  const bodyLines = parsed.sections.flatMap((section) =>
    section.lines.filter((line) => {
      const trimmed = line.trim()
      return trimmed && !trimmed.startsWith('%') && !trimmed.startsWith('\\section')
    }),
  )

  return (
    <div className="resume-document resume-document-rendered">
      <header className="resume-document-header">
        <div>
          <h3>{fullName}</h3>
          <p>{email}</p>
        </div>
        <div className="resume-document-meta">
          <span className="company-chip">{companyName}</span>
          <small>{jobTitle}</small>
        </div>
      </header>

      {parsed.summary && (
        <section className="resume-document-section">
          <h4>Summary</h4>
          <p>{parsed.summary}</p>
        </section>
      )}

      {parsed.skills.length > 0 && (
        <section className="resume-document-section">
          <h4>Skills</h4>
          <div className="tag-list">
            {parsed.skills.map((skill) => (
              <span key={skill}>{skill}</span>
            ))}
          </div>
        </section>
      )}

      {parsed.experienceBullets.length > 0 && (
        <section className="resume-document-section">
          <h4>Experience</h4>
          <ul className="resume-bullet-list">
            {parsed.experienceBullets.map((bullet) => (
              <li key={bullet}>{bullet}</li>
            ))}
          </ul>
        </section>
      )}

      {parsed.sections
        .filter((section) => {
          const lower = section.title.toLowerCase()
          return (
            !lower.includes('skill') &&
            !lower.includes('experience') &&
            !lower.includes('summary') &&
            section.title !== 'Header'
          )
        })
        .map((section) => (
          <section className="resume-document-section" key={section.title}>
            <h4>{section.title}</h4>
            <div className="resume-rendered-lines">
              {section.lines
                .map((line) => formatLineForDisplay(line))
                .filter(Boolean)
                .map((line, index) => (
                  <p className={line.startsWith('• ') ? 'resume-rendered-bullet' : undefined} key={`${section.title}-${index}`}>
                    {line}
                  </p>
                ))}
            </div>
          </section>
        ))}

      {bodyLines.length > 0 && parsed.sections.length === 0 && (
        <section className="resume-document-section">
          <div className="resume-rendered-lines">
            {bodyLines.map((line, index) => (
              <p key={index}>{formatLineForDisplay(line)}</p>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
