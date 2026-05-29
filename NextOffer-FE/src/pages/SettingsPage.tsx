import { useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { PanelHeader } from '../components/PanelHeader'
import type { CompanyWatch } from '../api/types'

type WatchFilterForm = {
  locationFilter: string
  keywordFilter: string
  departmentFilter: string
}

const emptyFilters = (): WatchFilterForm => ({
  locationFilter: '',
  keywordFilter: '',
  departmentFilter: '',
})

function filtersFromWatch(watch: CompanyWatch): WatchFilterForm {
  return {
    locationFilter: watch.locationFilter ?? '',
    keywordFilter: watch.keywordFilter ?? '',
    departmentFilter: watch.departmentFilter ?? '',
  }
}

function formatActiveFilters(watch: CompanyWatch) {
  const parts: string[] = []
  if (watch.locationFilter) {
    parts.push(`Location: ${watch.locationFilter}`)
  }
  if (watch.keywordFilter) {
    parts.push(`Keywords: ${watch.keywordFilter}`)
  }
  if (watch.departmentFilter) {
    parts.push(`Department: ${watch.departmentFilter}`)
  }
  return parts.length > 0 ? parts.join(' · ') : 'No filters (all jobs)'
}

export function SettingsPage() {
  const { watches, addWatch, updateWatchFilters, removeWatch, pollWatch } = useAppData()
  const [companyName, setCompanyName] = useState('')
  const [careerPageUrl, setCareerPageUrl] = useState('https://boards.greenhouse.io/stripe')
  const [newFilters, setNewFilters] = useState<WatchFilterForm>(emptyFilters)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editFilters, setEditFilters] = useState<WatchFilterForm>(emptyFilters)
  const [submitting, setSubmitting] = useState(false)
  const [pollingId, setPollingId] = useState<number | null>(null)
  const [savingFiltersId, setSavingFiltersId] = useState<number | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleAddWatch(event: React.FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setMessage(null)
    try {
      await addWatch(companyName, careerPageUrl, newFilters)
      setCompanyName('')
      setNewFilters(emptyFilters())
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
      if (editingId === id) {
        setEditingId(null)
      }
      setMessage('Company watch removed.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove watch')
    }
  }

  function startEditFilters(watch: CompanyWatch) {
    setEditingId(watch.id)
    setEditFilters(filtersFromWatch(watch))
  }

  async function handleSaveFilters(watchId: number) {
    setSavingFiltersId(watchId)
    setError(null)
    setMessage(null)
    try {
      await updateWatchFilters(watchId, editFilters)
      setEditingId(null)
      setMessage('Watch filters updated.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update filters')
    } finally {
      setSavingFiltersId(null)
    }
  }

  return (
    <section className="content-grid settings-layout">
      <article className="panel integration-panel">
        <PanelHeader title="Company watches" action={`${watches.length} active`} />
        <p className="panel-copy">
          Add target companies. Optional filters limit which jobs are saved — use comma-separated
          values for location, keywords (title/description), and department.
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
          <fieldset className="watch-filter-fieldset">
            <legend>Job filters (optional)</legend>
            <label>
              Locations
              <input
                placeholder="San Francisco, Remote, New York"
                value={newFilters.locationFilter}
                onChange={(event) =>
                  setNewFilters((current) => ({ ...current, locationFilter: event.target.value }))
                }
              />
            </label>
            <label>
              Keywords
              <input
                placeholder="treasury, backend, intern"
                value={newFilters.keywordFilter}
                onChange={(event) =>
                  setNewFilters((current) => ({ ...current, keywordFilter: event.target.value }))
                }
              />
            </label>
            <label>
              Departments
              <input
                placeholder="Engineering, Product, Operations"
                value={newFilters.departmentFilter}
                onChange={(event) =>
                  setNewFilters((current) => ({ ...current, departmentFilter: event.target.value }))
                }
              />
            </label>
          </fieldset>
          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Adding…' : 'Add watch'}
          </button>
        </form>

        {message && <p className="inline-message">{message}</p>}
        {error && <p className="form-error">{error}</p>}

        <div className="watch-list">
          {watches.map((watch) => (
            <div className="watch-row" key={watch.id}>
              <div className="watch-row-main">
                <strong>{watch.companyName}</strong>
                <span>{watch.careerPageUrl}</span>
                <small className="watch-filter-summary">{formatActiveFilters(watch)}</small>
                <small>
                  Last scan: {watch.lastCheckedAt ? new Date(watch.lastCheckedAt).toLocaleString() : 'Never'}
                  {watch.lastScanStatus ? ` · ${watch.lastScanStatus}` : ''}
                </small>

                {editingId === watch.id && (
                  <div className="watch-filter-edit">
                    <label>
                      Locations
                      <input
                        value={editFilters.locationFilter}
                        onChange={(event) =>
                          setEditFilters((current) => ({
                            ...current,
                            locationFilter: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label>
                      Keywords
                      <input
                        value={editFilters.keywordFilter}
                        onChange={(event) =>
                          setEditFilters((current) => ({
                            ...current,
                            keywordFilter: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label>
                      Departments
                      <input
                        value={editFilters.departmentFilter}
                        onChange={(event) =>
                          setEditFilters((current) => ({
                            ...current,
                            departmentFilter: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <div className="watch-filter-edit-actions">
                      <button
                        className="primary-button"
                        disabled={savingFiltersId === watch.id}
                        onClick={() => void handleSaveFilters(watch.id)}
                        type="button"
                      >
                        {savingFiltersId === watch.id ? 'Saving…' : 'Save filters'}
                      </button>
                      <button
                        className="ghost-button"
                        onClick={() => setEditingId(null)}
                        type="button"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}
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
                <button className="ghost-button" onClick={() => startEditFilters(watch)} type="button">
                  Edit filters
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
          <li>GET/POST/PUT/DELETE /api/watches and POST /api/watches/:id/poll</li>
          <li>GET /api/jobs and GET /api/jobs/:id</li>
          <li>Resume tailoring and tracker endpoints are still mock-only in the UI.</li>
        </ul>
      </article>
    </section>
  )
}
