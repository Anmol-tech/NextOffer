export type AuthUser = {
  id: number
  email: string
  fullName: string
  createdAt: string
}

export type AuthResponse = {
  token: string
  user: AuthUser
}

export type CompanyWatch = {
  id: number
  companyName: string
  careerPageUrl: string
  boardToken: string | null
  atsType: 'GREENHOUSE' | 'WORKDAY' | 'SMART_RECRUITERS' | 'LEVER' | 'CUSTOM_HTML'
  enabled: boolean
  locationFilter: string | null
  keywordFilter: string | null
  departmentFilter: string | null
  lastCheckedAt: string | null
  lastScanStatus: 'SUCCESS' | 'FAILED' | null
  lastErrorMessage: string | null
  createdAt: string
}

export type ApplicationStatus = 'NEW' | 'VIEWED' | 'APPLIED' | 'REJECTED'

export type JobPosting = {
  id: number
  companyWatchId: number
  externalId: string
  companyName: string
  title: string
  location: string | null
  applyUrl: string
  description: string | null
  firstSeenAt: string
  applicationStatus: ApplicationStatus
  statusUpdatedAt: string
}

export type CreateWatchRequest = {
  companyName: string
  careerPageUrl: string
  boardToken?: string
  atsType?: CompanyWatch['atsType']
  enabled?: boolean
  locationFilter?: string
  keywordFilter?: string
  departmentFilter?: string
}

export type UpdateWatchRequest = {
  companyName?: string
  careerPageUrl?: string
  boardToken?: string
  atsType?: CompanyWatch['atsType']
  enabled?: boolean
  locationFilter?: string
  keywordFilter?: string
  departmentFilter?: string
}

export type ApiProblem = {
  title?: string
  detail?: string
  status?: number
  errors?: Record<string, string>
}

export type ResumeOutputStatus = 'PDF_READY' | 'LATEX_ONLY' | 'FAILED'

export type ResumeSourceFormat = 'TEXT' | 'LATEX'

export type BaseResume = {
  id: number
  rawText: string
  updatedAt: string
  sourceFormat: ResumeSourceFormat
}

export type TailoredResumeContent = {
  summary: string
  skills: string[]
  experienceBullets: string[]
}

export type TailoredResumeSummary = {
  id: number
  jobPostingId: number
  jobTitle: string
  companyName: string
  outputStatus: ResumeOutputStatus
  summaryPreview: string | null
  createdAt: string
}

export type TailoredResumeDetail = {
  id: number
  jobPostingId: number
  jobTitle: string
  companyName: string
  content: TailoredResumeContent
  latexContent: string | null
  sourceFormat: ResumeSourceFormat
  outputStatus: ResumeOutputStatus
  pdfAvailable: boolean
  createdAt: string
}
