import { useMemo, useState } from 'react'
import { jobs, navItems } from './data/mockData'
import { DashboardPage } from './pages/DashboardPage'
import { JobsPage } from './pages/JobsPage'
import { ResumesPage } from './pages/ResumesPage'
import { SettingsPage } from './pages/SettingsPage'
import { TrackerPage } from './pages/TrackerPage'
import type { View } from './types'
import './App.css'

function App() {
  const [activeView, setActiveView] = useState<View>('dashboard')
  const [selectedJobId, setSelectedJobId] = useState(jobs[0].id)
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')

  const selectedJob = useMemo(
    () => jobs.find((job) => job.id === selectedJobId) ?? jobs[0],
    [selectedJobId],
  )

  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">
          <span className="brand-mark">NO</span>
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
          <span className="eyebrow">Backend handoff</span>
          <p>Mock data only. API boundaries are grouped in auth, jobs, resumes, and tracker surfaces.</p>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Student workspace</p>
            <h1>{pageTitle(activeView)}</h1>
          </div>
          <div className="topbar-actions">
            <button className="ghost-button" type="button" onClick={() => setActiveView('resumes')}>
              Upload resume
            </button>
            <button className="primary-button" type="button" onClick={() => setActiveView('resumes')}>
              Tailor resume
            </button>
          </div>
        </header>

        {activeView === 'dashboard' && (
          <DashboardPage selectedJob={selectedJob} onSelectJob={setSelectedJobId} onNavigate={setActiveView} />
        )}
        {activeView === 'jobs' && (
          <JobsPage selectedJob={selectedJob} selectedJobId={selectedJobId} onSelectJob={setSelectedJobId} />
        )}
        {activeView === 'resumes' && <ResumesPage selectedJob={selectedJob} />}
        {activeView === 'tracker' && <TrackerPage />}
        {activeView === 'settings' && (
          <SettingsPage authMode={authMode} onAuthModeChange={setAuthMode} />
        )}
      </section>
    </main>
  )
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
      return 'Prepare auth and API integration without calling the backend yet.'
    default:
      return 'Track roles, tailor resumes, and move faster.'
  }
}

export default App
