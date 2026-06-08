import { useState } from 'react'
import { useAppData } from '../context/AppDataContext'
import { PanelHeader } from '../components/PanelHeader'
import type { CompanyWatch } from '../api/types'

type WatchFilterForm = {
  locationFilter: string
  keywordFilter: string
  departmentFilter: string
}

type AtsOption = { value: CompanyWatch['atsType']; label: string; placeholder: string }

const ATS_OPTIONS: AtsOption[] = [
  {
    value: 'GREENHOUSE',
    label: 'Greenhouse',
    placeholder: 'https://boards.greenhouse.io/your-company',
  },
  {
    value: 'WORKDAY',
    label: 'Workday',
    placeholder: 'https://your-company.wd5.myworkdayjobs.com/en-US/career-site',
  },
  {
    value: 'SMART_RECRUITERS',
    label: 'SmartRecruiters',
    placeholder: 'https://jobs.smartrecruiters.com/YourCompany',
  },
  {
    value: 'LEVER',
    label: 'Lever',
    placeholder: 'https://jobs.lever.co/your-company',
  },
  {
    value: 'CUSTOM_HTML',
    label: 'Custom HTML',
    placeholder: 'https://your-company.com/careers',
  },
]

const DEMO_WATCH_PRESETS: Array<{
  label: string
  companyName: string
  atsType: CompanyWatch['atsType']
  careerPageUrl: string
  filters: WatchFilterForm
}> = [
  {
    label: 'Stripe',
    companyName: 'Stripe',
    atsType: 'GREENHOUSE',
    careerPageUrl: 'https://boards.greenhouse.io/stripe',
    filters: {
      locationFilter: 'San Francisco, Remote',
      keywordFilter: 'software, engineer',
      departmentFilter: 'Engineering',
    },
  },
  {
    label: 'Workday',
    companyName: 'Workday',
    atsType: 'WORKDAY',
    careerPageUrl: 'https://workday.wd5.myworkdayjobs.com/en-US/Workday',
    filters: {
      locationFilter: 'Pleasanton, Remote',
      keywordFilter: 'software, product',
      departmentFilter: 'Engineering',
    },
  },
  {
    label: 'Anthropic',
    companyName: 'Anthropic',
    atsType: 'GREENHOUSE',
    careerPageUrl: 'https://boards.greenhouse.io/anthropic',
    filters: {
      locationFilter: 'San Francisco, New York',
      keywordFilter: 'research, engineer',
      departmentFilter: 'Research',
    },
  },
  {
    label: 'Visa',
    companyName: 'Visa',
    atsType: 'SMART_RECRUITERS',
    careerPageUrl: 'https://jobs.smartrecruiters.com/Visa',
    filters: {
      locationFilter: 'Austin, Foster City',
      keywordFilter: 'software, platform',
      departmentFilter: 'Technology',
    },
  },
]

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
  return parts.length > 0 ? parts.join(' | ') : 'No filters'
}

function formatLastScan(watch: CompanyWatch) {
  if (!watch.lastCheckedAt) {
    return 'Never scanned'
  }
  return `Last scan ${new Date(watch.lastCheckedAt).toLocaleString()}`
}

export function SettingsPage() {
  const { watches, addWatch, updateWatchFilters, removeWatch, pollWatch } = useAppData()
  const [companyName, setCompanyName] = useState('')
  const [atsType, setAtsType] = useState<CompanyWatch['atsType']>('GREENHOUSE')
  const [careerPageUrl, setCareerPageUrl] = useState('')
  const [newFilters, setNewFilters] = useState<WatchFilterForm>(emptyFilters)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editFilters, setEditFilters] = useState<WatchFilterForm>(emptyFilters)
  const [submitting, setSubmitting] = useState(false)
  const [pollingId, setPollingId] = useState<number | null>(null)
  const [savingFiltersId, setSavingFiltersId] = useState<number | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const selectedAts = ATS_OPTIONS.find((o) => o.value === atsType) ?? ATS_OPTIONS[0]

  function handleAtsChange(value: CompanyWatch['atsType']) {
    setAtsType(value)
    setCareerPageUrl('')
  }

  function applyPreset(preset: (typeof DEMO_WATCH_PRESETS)[number]) {
    setCompanyName(preset.companyName)
    setAtsType(preset.atsType)
    setCareerPageUrl(preset.careerPageUrl)
    setNewFilters(preset.filters)
    setFiltersOpen(true)
    setError(null)
    setMessage(`Pre-filled ${preset.companyName} — review and click Add watch.`)
  }

  async function handleAddWatch(event: React.FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setMessage(null)
    try {
      await addWatch(companyName, careerPageUrl, newFilters, atsType)
      setCompanyName('')
      setCareerPageUrl('')
      setAtsType('GREENHOUSE')
      setNewFilters(emptyFilters())
      setFiltersOpen(false)
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
    <section className="content-grid settings-layout layout-fill">
      <article className="panel settings-watch-panel panel-scroll-column">
        <div className="settings-watch-top panel-scroll-header">
          <PanelHeader title="Company watches" action={`${watches.length} active`} />

          <form className="watch-form watch-form-card" onSubmit={handleAddWatch}>
          <div className="watch-form-header">
            <div>
              <span className="eyebrow">New watch</span>
              <h3>Add a career page</h3>
            </div>
            <button className="primary-button" disabled={submitting} type="submit">
              {submitting ? 'Adding...' : 'Add watch'}
            </button>
          </div>

          <div className="watch-preset-row">
            <span className="watch-preset-label">Quick fill for demo</span>
            <div className="watch-preset-buttons">
              {DEMO_WATCH_PRESETS.map((preset) => (
                <button
                  className="ghost-button watch-preset-button"
                  key={preset.label}
                  onClick={() => applyPreset(preset)}
                  type="button"
                >
                  {preset.label}
                </button>
              ))}
            </div>
          </div>

          <div className="watch-form-primary">
            <label>
              Company
              <input
                placeholder="Company name"
                value={companyName}
                onChange={(event) => setCompanyName(event.target.value)}
                required
              />
            </label>
            <label>
              ATS / Job board type
              <select
                value={atsType}
                onChange={(event) => handleAtsChange(event.target.value as CompanyWatch['atsType'])}
              >
                {ATS_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Career page URL
              <input
                placeholder={selectedAts.placeholder}
                value={careerPageUrl}
                onChange={(event) => setCareerPageUrl(event.target.value)}
                required
              />
            </label>
          </div>

          <details
            className="watch-filter-details"
            open={filtersOpen}
            onToggle={(event) => setFiltersOpen(event.currentTarget.open)}
          >
            <summary>Optional filters</summary>
            <div className="watch-filter-grid">
              <label>
                Locations
                <input
                  placeholder="San Francisco, Remote"
                  value={newFilters.locationFilter}
                  onChange={(event) =>
                    setNewFilters((current) => ({ ...current, locationFilter: event.target.value }))
                  }
                />
              </label>
              <label>
                Keywords
                <input
                  placeholder="backend, intern"
                  value={newFilters.keywordFilter}
                  onChange={(event) =>
                    setNewFilters((current) => ({ ...current, keywordFilter: event.target.value }))
                  }
                />
              </label>
              <label>
                Departments
                <input
                  placeholder="Engineering, Product"
                  value={newFilters.departmentFilter}
                  onChange={(event) =>
                    setNewFilters((current) => ({ ...current, departmentFilter: event.target.value }))
                  }
                />
              </label>
            </div>
          </details>
        </form>

        {message && <p className="inline-message">{message}</p>}
        {error && <p className="form-error">{error}</p>}

          <div className="settings-section-header">
            <div>
              <span className="eyebrow">Saved watches</span>
              <h3>Career pages being monitored</h3>
            </div>
          </div>
        </div>

        <div className="watch-list panel-scroll-body">
          {watches.map((watch) => (
            <div className="watch-row" key={watch.id}>
              <div className="watch-row-main">
                <div className="watch-row-title">
                  <strong>{watch.companyName}</strong>
                  <span className={`status status-${watch.enabled ? 'applied' : 'rejected'}`}>
                    {watch.enabled ? 'Active' : 'Paused'}
                  </span>
                  <span className="status">
                    {ATS_OPTIONS.find((o) => o.value === watch.atsType)?.label ?? watch.atsType}
                  </span>
                </div>
                <span>{watch.careerPageUrl}</span>
                <small className="watch-filter-summary">{formatActiveFilters(watch)}</small>
                <small>
                  {formatLastScan(watch)}
                  {watch.lastScanStatus ? ` | ${watch.lastScanStatus}` : ''}
                </small>
                {watch.lastScanStatus === 'FAILED' && watch.lastErrorMessage && (
                  <small className="watch-error">{watch.lastErrorMessage}</small>
                )}

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
                        {savingFiltersId === watch.id ? 'Saving...' : 'Save filters'}
                      </button>
                      <button className="ghost-button" onClick={() => setEditingId(null)} type="button">
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
                  {pollingId === watch.id ? 'Polling...' : 'Poll'}
                </button>
                <button className="ghost-button" onClick={() => startEditFilters(watch)} type="button">
                  Filters
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
    </section>
  )
}
