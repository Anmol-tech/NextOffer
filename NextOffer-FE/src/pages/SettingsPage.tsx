import { PanelHeader } from '../components/PanelHeader'
import { integrationNotes } from '../data/mockData'

type SettingsPageProps = {
  authMode: 'login' | 'register'
  onAuthModeChange: (mode: 'login' | 'register') => void
}

export function SettingsPage({ authMode, onAuthModeChange }: SettingsPageProps) {
  return (
    <section className="content-grid settings-layout">
      <article className="panel auth-panel">
        <PanelHeader title="Auth screen mock" action={authMode === 'login' ? 'Login' : 'Register'} />
        <div className="segmented-control">
          <button className={authMode === 'login' ? 'active' : ''} onClick={() => onAuthModeChange('login')} type="button">
            Login
          </button>
          <button className={authMode === 'register' ? 'active' : ''} onClick={() => onAuthModeChange('register')} type="button">
            Register
          </button>
        </div>
        <form className="auth-form">
          {authMode === 'register' && (
            <label>
              Full name
              <input placeholder="Alex Morgan" />
            </label>
          )}
          <label>
            Email
            <input placeholder="alex@university.edu" />
          </label>
          <label>
            Password
            <input placeholder="Minimum 8 characters" type="password" />
          </label>
          <button className="primary-button" type="button">
            {authMode === 'login' ? 'Sign in' : 'Create account'}
          </button>
        </form>
      </article>

      <article className="panel integration-panel">
        <PanelHeader title="Integration checklist" action="Ready for API" />
        <ul>
          {integrationNotes.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </article>
    </section>
  )
}
