import { useMemo, useState } from 'react'
import { navItems } from './data/mockData'
import { useAppData } from './context/AppDataContext'
import { useAuth } from './context/AuthContext'
import { AuthScreen } from './pages/AuthScreen'
import { DashboardPage } from './pages/DashboardPage'
import { JobsPage } from './pages/JobsPage'
import { ResumesPage } from './pages/ResumesPage'
import { SettingsPage } from './pages/SettingsPage'
import { TrackerPage } from './pages/TrackerPage'
import type { Job, View } from './types'
import './App.css'

function App() {
  const { user, loading: authLoading, logout } = useAuth()
  const { jobs, loading: dataLoading, error } = useAppData()
  const [activeView, setActiveView] = useState<View>('dashboard')
  const [selectedJobId, setSelectedJobId] = useState<string | null>(null)

  const selectedJob = useMemo(() => {
    if (jobs.length === 0) {
      return emptyJob()
    }
    return jobs.find((job) => job.id === selectedJobId) ?? jobs[0]
  }, [jobs, selectedJobId])

  if (authLoading) {
    return (
      <main className="auth-shell">
        <p className="loading-state">Loading session…</p>
      </main>
    )
  }

  if (!user) {
    return <AuthScreen />
  }

  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">
          <img className="brand-logo" src="/nextoffer-logo.png" alt="NextOffer logo" />
          <div>
            <strong>NextOffer</strong>
            <span>AI application assistant</span>
          </div>
        </div>

        <nav className="nav-list">
          {navItems.map((item) => (
            <button
              className={activeView === item.id ? 'active' : ''}
              key={item.id}
              onClick={() => setActiveView(item.id)}
              type="button"
            >
              <span>{item.icon}</span>
              {item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-panel">
          <span className="eyebrow">Signed in</span>
          <p>
            <strong>{user.fullName}</strong>
            <br />
            {user.email}
          </p>
          <button className="ghost-button sidebar-logout" onClick={logout} type="button">
            Sign out
          </button>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Student workspace</p>
            <h1>{pageTitle(activeView)}</h1>
          </div>
          <div className="topbar-actions">
            <button className="ghost-button" type="button" onClick={() => setActiveView('settings')}>
              Manage watches
            </button>
            <button className="primary-button" type="button" onClick={() => setActiveView('jobs')}>
              View jobs
            </button>
          </div>
        </header>

        {(dataLoading || error) && (
          <div className={`banner ${error ? 'banner-error' : ''}`}>
            {error ?? 'Syncing jobs and company watches…'}
          </div>
        )}

        {activeView === 'dashboard' && (
          <DashboardPage
            jobs={jobs}
            selectedJob={selectedJob}
            onSelectJob={setSelectedJobId}
            onNavigate={setActiveView}
          />
        )}
        {activeView === 'jobs' && (
          <JobsPage
            jobs={jobs}
            selectedJob={selectedJob}
            selectedJobId={selectedJob.id}
            onSelectJob={setSelectedJobId}
          />
        )}
        {activeView === 'resumes' && <ResumesPage selectedJob={selectedJob} />}
        {activeView === 'tracker' && <TrackerPage jobs={jobs} />}
        {activeView === 'settings' && <SettingsPage />}
      </section>
    </main>
  )
}

function emptyJob(): Job {
  return {
    id: 'empty',
    company: 'No company',
    role: 'Add a company watch to discover jobs',
    location: '—',
    match: 0,
    status: 'New',
    firstSeen: '—',
    applyUrl: '#',
    salary: '—',
    stack: [],
    highlights: ['Create a company watch in Settings, then poll or wait for the observer.'],
  }
}

function pageTitle(view: View) {
  switch (view) {
    case 'jobs':
      return 'Review matched jobs and choose the next resume target.'
    case 'resumes':
      return 'Manage base resumes, versions, and tailoring runs.'
    case 'tracker':
      return 'Move each application through a simple, visible pipeline.'
    case 'settings':
      return 'Manage company watches and account settings.'
    default:
      return 'Track roles, tailor resumes, and move faster.'
  }
}

export default App
