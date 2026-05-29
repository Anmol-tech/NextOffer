import { useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { PanelHeader } from '../components/PanelHeader'
import type { CompanyWatch } from '../api/types'

export function SettingsPage() {
  const { watches, addWatch, removeWatch, pollWatch } = useAppData()
  const [companyName, setCompanyName] = useState('')
  const [careerPageUrl, setCareerPageUrl] = useState('https://boards.greenhouse.io/stripe')
  const [submitting, setSubmitting] = useState(false)
  const [pollingId, setPollingId] = useState<number | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleAddWatch(event: React.FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setMessage(null)
    try {
      await addWatch(companyName, careerPageUrl)
      setCompanyName('')
      setMessage('Company watch added.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add watch')
    } finally {
      setSubmitting(false)
    }
  }

  async function handlePoll(watch: CompanyWatch) {
    setPollingId(watch.id)
    setError(null)
    setMessage(null)
    try {
      const count = await pollWatch(watch.id)
      setMessage(`${watch.companyName}: ${count} new job(s) found.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Poll failed')
    } finally {
      setPollingId(null)
    }
  }

  async function handleRemove(id: number) {
    setError(null)
    try {
      await removeWatch(id)
      setMessage('Company watch removed.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove watch')
    }
  }

  return (
    <section className="content-grid settings-layout">
      <article className="panel integration-panel">
        <PanelHeader title="Company watches" action={`${watches.length} active`} />
        <p className="panel-copy">
          Add target companies. The backend observer polls Greenhouse boards and stores new jobs.
        </p>

        <form className="watch-form" onSubmit={handleAddWatch}>
          <label>
            Company name
            <input
              placeholder="Stripe"
              value={companyName}
              onChange={(event) => setCompanyName(event.target.value)}
              required
            />
          </label>
          <label>
            Career page URL
            <input
              placeholder="https://boards.greenhouse.io/stripe"
              value={careerPageUrl}
              onChange={(event) => setCareerPageUrl(event.target.value)}
              required
            />
          </label>
          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Adding…' : 'Add watch'}
          </button>
        </form>

        {message && <p className="inline-message">{message}</p>}
        {error && <p className="form-error">{error}</p>}

        <div className="watch-list">
          {watches.map((watch) => (
            <div className="watch-row" key={watch.id}>
              <div>
                <strong>{watch.companyName}</strong>
                <span>{watch.careerPageUrl}</span>
                <small>
                  Last scan: {watch.lastCheckedAt ? new Date(watch.lastCheckedAt).toLocaleString() : 'Never'}
                  {watch.lastScanStatus ? ` · ${watch.lastScanStatus}` : ''}
                </small>
              </div>
              <div className="watch-actions">
                <button
                  className="ghost-button"
                  disabled={pollingId === watch.id}
                  onClick={() => void handlePoll(watch)}
                  type="button"
                >
                  {pollingId === watch.id ? 'Polling…' : 'Poll now'}
                </button>
                <button className="ghost-button" onClick={() => void handleRemove(watch.id)} type="button">
                  Remove
                </button>
              </div>
            </div>
          ))}
          {watches.length === 0 && <p className="empty-state">No company watches yet.</p>}
        </div>
      </article>

      <article className="panel integration-panel">
        <PanelHeader title="Connected APIs" action="Live" />
        <ul className="integration-list">
          <li>POST /api/auth/register, /api/auth/login, GET /api/auth/me</li>
          <li>GET/POST/DELETE /api/watches and POST /api/watches/:id/poll</li>
          <li>GET /api/jobs and GET /api/jobs/:id</li>
          <li>Resume tailoring and tracker endpoints are still mock-only in the UI.</li>
        </ul>
      </article>
    </section>
  )
}
