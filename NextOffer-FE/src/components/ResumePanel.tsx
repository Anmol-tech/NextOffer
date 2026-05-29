import { useRef, useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import type { ResumeSourceFormat } from '../api/types'
import type { ResumeViewerTarget } from '../lib/resumeViewer'
import { PanelHeader } from './PanelHeader'

function formatCreated(iso: string) {
  return new Date(iso).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

function statusLabel(status: string) {
  switch (status) {
    case 'PDF_READY':
      return 'PDF ready'
    case 'LATEX_ONLY':
      return 'LaTeX only'
    case 'FAILED':
      return 'Failed'
    default:
      return status
  }
}

function formatLabel(format: ResumeSourceFormat) {
  return format === 'LATEX' ? 'LaTeX' : 'Text'
}

function detectFormat(text: string): ResumeSourceFormat {
  const sample = text.trim()
  if (
    sample.includes('\\documentclass') ||
    sample.includes('\\begin{document}') ||
    (sample.includes('\\section') && sample.includes('\\item'))
  ) {
    return 'LATEX'
  }
  return 'TEXT'
}

export function ResumePanel({
  full = false,
  viewerTarget = null,
  onViewTarget,
}: {
  full?: boolean
  viewerTarget?: ResumeViewerTarget | null
  onViewTarget?: (target: ResumeViewerTarget) => void
}) {
  const {
    baseResume,
    tailoredResumes,
    resumeLoading,
    resumeError,
    saveBaseResume,
    downloadResume,
  } = useAppData()
  const [draft, setDraft] = useState('')
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const textFileInputRef = useRef<HTMLInputElement>(null)
  const texFileInputRef = useRef<HTMLInputElement>(null)

  const versions = full ? tailoredResumes : tailoredResumes.slice(0, 3)
  const draftFormat = detectFormat(draft)

  function startEditing() {
    setDraft(baseResume?.rawText ?? '')
    setEditing(true)
    setMessage(null)
  }

  async function persistResume(rawText: string, fileName?: string) {
    if (!rawText.trim()) {
      setMessage('Paste or upload resume content before saving.')
      return
    }
    setSaving(true)
    setMessage(null)
    try {
      await saveBaseResume(rawText.trim())
      setEditing(false)
      const format = detectFormat(rawText)
      setMessage(
        fileName
          ? `Saved ${fileName} as ${formatLabel(format)} base resume.`
          : `Saved ${formatLabel(format)} base resume.`,
      )
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  async function handleSave() {
    await persistResume(draft)
  }

  async function handleFileUpload(file: File) {
    const text = await file.text()
    setDraft(text)
    setEditing(true)
    await persistResume(text, file.name)
  }

  return (
    <article className="panel resume-panel">
      <PanelHeader
        title="Resume workspace"
        action={editing ? 'Cancel' : baseResume ? 'Edit base' : 'Add base'}
        onAction={() => (editing ? setEditing(false) : startEditing())}
      />

      {resumeError && <p className="inline-message inline-message-error">{resumeError}</p>}
      {message && <p className="inline-message">{message}</p>}

      {editing ? (
        <div className="resume-editor">
          <div className="resume-format-row">
            <span className="eyebrow">Input format</span>
            <span className={`format-chip format-chip-${draftFormat.toLowerCase()}`}>
              {formatLabel(draftFormat)}
            </span>
          </div>
          <textarea
            className="resume-textarea"
            onChange={(event) => setDraft(event.target.value)}
            placeholder={
              draftFormat === 'LATEX'
                ? 'Paste LaTeX resume source (.tex). Skills and Experience sections with \\item entries are parsed automatically.'
                : 'Paste plain-text resume content. Use a Skills section and bullet lines starting with -.'
            }
            rows={full ? 12 : 8}
            value={draft}
          />
          <div className="resume-editor-actions">
            <input
              accept=".txt,.md,.text"
              hidden
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file) {
                  void handleFileUpload(file)
                }
                event.target.value = ''
              }}
              ref={textFileInputRef}
              type="file"
            />
            <input
              accept=".tex"
              hidden
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file) {
                  void handleFileUpload(file)
                }
                event.target.value = ''
              }}
              ref={texFileInputRef}
              type="file"
            />
            <button className="ghost-button" onClick={() => textFileInputRef.current?.click()} type="button">
              Upload text file
            </button>
            <button className="ghost-button" onClick={() => texFileInputRef.current?.click()} type="button">
              Upload .tex file
            </button>
            <button className="primary-button" disabled={saving} onClick={() => void handleSave()} type="button">
              {saving ? 'Saving…' : 'Save base resume'}
            </button>
          </div>
        </div>
      ) : (
        <div className="upload-zone resume-upload-zone">
          <span>{baseResume?.sourceFormat === 'LATEX' ? 'TEX' : 'TXT'}</span>
          <div>
            {baseResume ? (
              <>
                <strong>Base resume saved ({formatLabel(baseResume.sourceFormat)})</strong>
                <p>
                  Updated {formatCreated(baseResume.updatedAt)} — parsed sections are ready for tailoring.
                </p>
              </>
            ) : (
              <>
                <strong>No base resume yet</strong>
                <p>Paste plain text or upload a .tex file to start tailoring.</p>
              </>
            )}
          </div>
          <div className="upload-zone-actions">
            {baseResume && onViewTarget && (
              <button
                className="ghost-button"
                onClick={() => onViewTarget({ kind: 'base' })}
                type="button"
              >
                View base
              </button>
            )}
            <input
              accept=".txt,.md,.text"
              hidden
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file) {
                  void handleFileUpload(file)
                }
                event.target.value = ''
              }}
              ref={textFileInputRef}
              type="file"
            />
            <input
              accept=".tex"
              hidden
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file) {
                  void handleFileUpload(file)
                }
                event.target.value = ''
              }}
              ref={texFileInputRef}
              type="file"
            />
            <button className="ghost-button" disabled={saving} onClick={() => textFileInputRef.current?.click()} type="button">
              Upload text
            </button>
            <button className="ghost-button" disabled={saving} onClick={() => texFileInputRef.current?.click()} type="button">
              Upload .tex
            </button>
          </div>
        </div>
      )}

      <div className="version-list">
        {resumeLoading && versions.length === 0 && (
          <p className="inline-message">Loading tailored versions…</p>
        )}
        {!resumeLoading && versions.length === 0 && (
          <p className="inline-message">Generate a tailored resume from a job to see versions here.</p>
        )}
        {versions.map((version) => (
          <div className="version-row" key={version.id}>
            <div>
              <strong>{version.jobTitle}</strong>
              <span>
                {version.companyName} — {formatCreated(version.createdAt)}
              </span>
            </div>
            <div className="version-actions">
              <small>{statusLabel(version.outputStatus)}</small>
              <div className="version-downloads">
                {onViewTarget && (
                  <button
                    className={`ghost-button ghost-button-small${viewerTarget?.kind === 'tailored' && viewerTarget.id === version.id ? ' active' : ''}`}
                    onClick={() => onViewTarget({ kind: 'tailored', id: version.id })}
                    type="button"
                  >
                    View
                  </button>
                )}
                {(version.outputStatus === 'PDF_READY' || version.outputStatus === 'LATEX_ONLY') && (
                  <>
                    {version.outputStatus === 'PDF_READY' && (
                      <button
                        className="ghost-button ghost-button-small"
                        onClick={() => void downloadResume(version.id, 'pdf')}
                        type="button"
                      >
                        PDF
                      </button>
                    )}
                    <button
                      className="ghost-button ghost-button-small"
                      onClick={() => void downloadResume(version.id, 'latex')}
                      type="button"
                    >
                      LaTeX
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </article>
  )
}
