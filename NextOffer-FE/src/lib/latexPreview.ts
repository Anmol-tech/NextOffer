export type ParsedResumePreview = {
  summary: string
  skills: string[]
  experienceBullets: string[]
  sections: { title: string; lines: string[] }[]
}

const SECTION_HEADER = /\\section\*?\{([^}]+)\}/
const COMMENT_SECTION = /%\s*-+\s*([A-Z0-9][A-Z0-9\s/&\-]+?)\s*-+/i
const ITEM_LINE = /^\s*\\item(?:\[[^\]]*\])?\s*(.*)$/

function stripLatex(input: string) {
  return input
    .replace(/\\textbf\{([^{}]*)\}/g, '$1')
    .replace(/\\textit\{([^{}]*)\}/g, '$1')
    .replace(/\\href\{[^}]*\}\{([^{}]*)\}/g, '$1')
    .replace(/\\[a-zA-Z@]+(?:\[[^\]]*\])?\{([^{}]*)\}/g, '$1')
    .replace(/[{}]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

function detectSectionTitle(line: string) {
  const trimmed = line.trim()
  const section = trimmed.match(SECTION_HEADER)
  if (section) {
    return section[1].trim()
  }
  const comment = trimmed.match(COMMENT_SECTION)
  if (comment) {
    return comment[1].trim()
  }
  return null
}

export function parseLatexForPreview(rawText: string): ParsedResumePreview {
  const begin = rawText.indexOf('\\begin{document}')
  const end = rawText.indexOf('\\end{document}')
  const body =
    begin >= 0
      ? rawText.slice(begin + '\\begin{document}'.length, end > begin ? end : undefined)
      : rawText

  const lines = body.split(/\r?\n/)
  const sections: { title: string; lines: string[] }[] = []
  let currentTitle = 'Header'
  let currentLines: string[] = []
  const skills: string[] = []
  const experienceBullets: string[] = []
  const summaryParts: string[] = []

  function flushSection() {
    if (currentLines.length === 0) {
      return
    }
    sections.push({ title: currentTitle, lines: [...currentLines] })
  }

  for (const line of lines) {
    const title = detectSectionTitle(line)
    if (title) {
      flushSection()
      currentTitle = title
      currentLines = [line]
      continue
    }

    currentLines.push(line)
    const trimmed = line.trim()
    const item = trimmed.match(ITEM_LINE)
    if (item) {
      const text = stripLatex(item[1])
      if (text) {
        const lower = currentTitle.toLowerCase()
        if (lower.includes('skill')) {
          skills.push(text)
        } else if (lower.includes('experience') || lower.includes('employment')) {
          experienceBullets.push(text)
        }
      }
      continue
    }

    if (currentTitle === 'Header' && trimmed && !trimmed.startsWith('%') && !trimmed.startsWith('\\')) {
      const text = stripLatex(trimmed)
      if (text) {
        summaryParts.push(text)
      }
    }
  }

  flushSection()

  return {
    summary: summaryParts.join(' '),
    skills,
    experienceBullets,
    sections,
  }
}

export function formatLineForDisplay(line: string) {
  const trimmed = line.trim()
  if (!trimmed || trimmed.startsWith('%')) {
    return ''
  }
  const item = trimmed.match(ITEM_LINE)
  const content = item ? item[1] : trimmed
  const cleaned = stripLatex(content)
  return item ? `• ${cleaned}` : cleaned
}

export function parsePlainTextForPreview(rawText: string): ParsedResumePreview {
  const lines = rawText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  const summary = lines[0] ?? ''
  const skills: string[] = []
  const bullets: string[] = []
  let inSkills = false

  for (let i = 1; i < lines.length; i++) {
    const line = lines[i]
    const lower = line.toLowerCase()
    if (lower.startsWith('skills:') || lower === 'skills') {
      inSkills = true
      const inline = line.includes(':') ? line.slice(line.indexOf(':') + 1).trim() : ''
      if (inline) {
        skills.push(...inline.split(/[,;|]/).map((part) => part.trim()).filter(Boolean))
      }
      continue
    }
    if (lower.startsWith('experience:') || lower === 'experience') {
      inSkills = false
      continue
    }
    if (line.startsWith('-') || line.startsWith('*') || line.startsWith('•')) {
      inSkills = false
      bullets.push(line.replace(/^[-*•]\s+/, '').trim())
      continue
    }
    if (inSkills) {
      skills.push(...line.split(/[,;|]/).map((part) => part.trim()).filter(Boolean))
    } else if (line.length > 20) {
      bullets.push(line)
    }
  }

  return {
    summary,
    skills,
    experienceBullets: bullets,
    sections: [{ title: 'Resume', lines }],
  }
}
