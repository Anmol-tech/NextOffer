import type { Job, ResumeVersion, View } from '../types'

export const navItems: { id: View; label: string; icon: string }[] = [
  { id: 'dashboard', label: 'Dashboard', icon: 'DB' },
  { id: 'jobs', label: 'Jobs', icon: 'JB' },
  { id: 'resumes', label: 'Resumes', icon: 'RS' },
  { id: 'tracker', label: 'Tracker', icon: 'TR' },
  { id: 'settings', label: 'Settings', icon: 'ST' },
]

export const jobs: Job[] = [
  {
    id: 'stripe-swe-intern',
    company: 'Stripe',
    role: 'Software Engineer Intern',
    location: 'San Francisco, CA',
    match: 92,
    status: 'Applied',
    firstSeen: 'Today',
    applyUrl: 'stripe.com/jobs',
    salary: '$58/hr',
    stack: ['Java', 'React', 'SQL', 'APIs'],
    highlights: [
      'Backend services and product infrastructure',
      'Strong overlap with Java, SQL, and API project work',
      'Resume version generated and ready for final review',
    ],
  },
  {
    id: 'databricks-new-grad',
    company: 'Databricks',
    role: 'New Grad Software Engineer',
    location: 'Mountain View, CA',
    match: 88,
    status: 'Viewed',
    firstSeen: 'Yesterday',
    applyUrl: 'databricks.com/company/careers',
    salary: '$145k',
    stack: ['Python', 'Spark', 'Distributed Systems', 'Testing'],
    highlights: [
      'Distributed systems language appears in coursework',
      'Needs stronger ordering of data platform experience',
      'Tailoring draft queued for review',
    ],
  },
  {
    id: 'figma-frontend',
    company: 'Figma',
    role: 'Frontend Engineering Intern',
    location: 'Remote',
    match: 84,
    status: 'New',
    firstSeen: '2 days ago',
    applyUrl: 'figma.com/careers',
    salary: '$52/hr',
    stack: ['TypeScript', 'React', 'Design Systems', 'Accessibility'],
    highlights: [
      'Frontend stack aligns with React and TypeScript projects',
      'Could emphasize UI polish and accessibility testing',
      'Not yet added to application tracker',
    ],
  },
  {
    id: 'roblox-backend',
    company: 'Roblox',
    role: 'Backend Software Engineer Intern',
    location: 'San Mateo, CA',
    match: 78,
    status: 'New',
    firstSeen: '3 days ago',
    applyUrl: 'careers.roblox.com',
    salary: '$55/hr',
    stack: ['Java', 'PostgreSQL', 'Services', 'Observability'],
    highlights: [
      'Backend keyword match is healthy',
      'Resume needs more reliability and monitoring language',
      'Direct apply link available',
    ],
  },
]

export const resumeVersions: ResumeVersion[] = [
  {
    id: 'stripe-resume',
    title: 'Stripe SWE Intern',
    target: 'Software Engineer Intern',
    created: 'May 29',
    match: 92,
    state: 'PDF ready',
  },
  {
    id: 'databricks-resume',
    title: 'Databricks New Grad',
    target: 'New Grad Software Engineer',
    created: 'May 28',
    match: 88,
    state: 'Needs review',
  },
  {
    id: 'base-resume',
    title: 'Base Resume',
    target: 'General SWE',
    created: 'May 25',
    match: 71,
    state: 'Source',
  },
]

export const activity = [
  'Stripe tailored resume generated from base resume.',
  'Databricks role moved to Viewed.',
  'Figma internship discovered from career page watch.',
  'Base resume parsed into skills, projects, and experience sections.',
]

export const integrationNotes = [
  'Auth can wire to POST /api/auth/register, POST /api/auth/login, and GET /api/auth/me.',
  'Job discovery UI expects a normalized JobPostingDto shape plus optional match score.',
  'Resume tailoring UI expects upload, tailor, version list, and PDF download endpoints.',
  'Tracker controls expect status mutation endpoints for New, Viewed, Applied, and Rejected.',
]
