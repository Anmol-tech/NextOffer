export type View = 'dashboard' | 'jobs' | 'resumes' | 'tracker' | 'settings'

export type JobStatus = 'New' | 'Viewed' | 'Applied' | 'Rejected'

export type Job = {
  id: string
  company: string
  role: string
  location: string
  match: number
  status: JobStatus
  firstSeen: string
  applyUrl: string
  salary: string
  stack: string[]
  highlights: string[]
}

export type ResumeVersion = {
  id: string
  title: string
  target: string
  created: string
  match: number
  state: string
}
