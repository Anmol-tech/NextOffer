import { ResumeViewer } from './ResumeViewer'
import type { ResumeViewerTarget } from '../lib/resumeViewer'

type ResumeViewerModalProps = {
  target: ResumeViewerTarget | null
  onClose: () => void
}

export function ResumeViewerModal({ target, onClose }: ResumeViewerModalProps) {
  if (!target) {
    return null
  }

  return (
    <div className="resume-viewer-modal" role="dialog" aria-modal="true" aria-label="Resume viewer">
      <button className="resume-viewer-backdrop" onClick={onClose} type="button" aria-label="Close resume viewer" />
      <div className="resume-viewer-modal-content">
        <ResumeViewer target={target} onClose={onClose} />
      </div>
    </div>
  )
}
