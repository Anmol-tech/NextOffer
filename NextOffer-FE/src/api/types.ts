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
  atsType: 'GREENHOUSE' | 'LEVER' | 'CUSTOM_HTML'
  enabled: boolean
  lastCheckedAt: string | null
  lastScanStatus: 'SUCCESS' | 'FAILED' | null
  lastErrorMessage: string | null
  createdAt: string
}

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
}

export type CreateWatchRequest = {
  companyName: string
  careerPageUrl: string
  boardToken?: string
  atsType?: CompanyWatch['atsType']
  enabled?: boolean
}

export type ApiProblem = {
  title?: string
  detail?: string
  status?: number
  errors?: Record<string, string>
}
