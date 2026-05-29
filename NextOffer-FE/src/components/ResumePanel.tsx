import { resumeVersions } from '../data/mockData'
import { PanelHeader } from './PanelHeader'

export function ResumePanel({ full = false }: { full?: boolean }) {
  return (
    <article className="panel resume-panel">
      <PanelHeader title="Resume workspace" action="Manage" />
      <div className="upload-zone">
        <span>PDF</span>
        <div>
          <strong>Base resume uploaded</strong>
          <p>Parsed sections are ready for ATS-friendly LaTeX insertion.</p>
        </div>
      </div>
      <div className="version-list">
        {(full ? resumeVersions : resumeVersions.slice(0, 3)).map((version) => (
          <div className="version-row" key={version.id}>
            <div>
              <strong>{version.title}</strong>
              <span>{version.target} - {version.created}</span>
            </div>
            <div>
              <span>{version.match}%</span>
              <small>{version.state}</small>
            </div>
          </div>
        ))}
      </div>
    </article>
  )
}
