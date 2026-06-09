# NextOffer

AI-powered job application assistant for students: watch company career pages, detect new software roles, tailor resumes per job, and track application status.

| Module | Stack | Folder |
|--------|-------|--------|
| Backend | Spring Boot 4, Java 21, PostgreSQL, JPA | `NextOffer/` |
| Frontend | React 19, TypeScript, Vite | `NextOffer-FE/` |

**Live demo**

| Service | URL |
|---------|-----|
| Frontend | https://next-offer-lake.vercel.app |
| Backend API | https://nextoffer.onrender.com |

> **Graders:** see **[GRADER_SETUP.md](GRADER_SETUP.md)** for a 10-minute run guide. Copy `.env.example` → `.env` before starting.

---

## Features

- Register / login with JWT
- Watch career pages (Greenhouse, Workday, SmartRecruiters)
- Poll for new jobs with keyword, location, and department filters
- Tailor resumes per job (AI via OpenRouter, or rule-based fallback)
- Export LaTeX / PDF resumes
- Application tracker (NEW → VIEWED → APPLIED → REJECTED)

---

## Prerequisites

- **Java 21**
- **Node.js 18+** and npm
- **PostgreSQL** (local or hosted, e.g. Neon)
- **pdflatex** (optional, for local PDF compile) — included in the backend Docker image for Render

---

## Run locally

### 1. Database

Create a PostgreSQL database:

```sql
CREATE DATABASE "next-offer";
```

### 2. Environment variables

From the **repo root**:

```bash
cp .env.example .env
```

Edit `.env` with at least:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/next-offer
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
JWT_SECRET=your-long-random-secret-at-least-32-characters
APP_CORS_ORIGINS=http://localhost:5173
```

Optional: set `OPENROUTER_API_KEY` for AI resume tailoring. Without it, rule-based tailoring still works.

> Spring Boot loads `.env` from the repo root when you run from `NextOffer/`. Do **not** commit `.env`.

### 3. Backend

```bash
cd NextOffer
./mvnw spring-boot:run
```

API runs at **http://localhost:8080**.

### 4. Frontend

In a second terminal:

```bash
cd NextOffer-FE
npm install
npm run dev
```

App runs at **http://localhost:5173**. Vite proxies `/api` to the backend — no extra frontend env needed for local dev.

### 5. First-time app flow

1. Open http://localhost:5173 and **register** an account
2. **Settings** → upload or paste a base resume
3. **Settings** → add a company watch (or use a quick-fill preset), then **Poll**
4. **Jobs** → select a job → **Generate tailored resume**
5. **Tracker** → update application status

---

## Run tests

**Backend** (from `NextOffer/`):

```bash
./mvnw test
```

**Frontend build** (from `NextOffer-FE/`):

```bash
npm run build
```

---

## Run with Docker (backend only)

From `NextOffer/`:

```bash
docker build -t nextoffer .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/neondb?sslmode=require \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  -e JWT_SECRET=your-long-random-secret-at-least-32-characters \
  -e APP_CORS_ORIGINS=http://localhost:5173 \
  nextoffer
```

The image includes LaTeX for PDF generation (`APP_RESUME_COMPILE_PDF=true` by default).

---

## Design patterns (course requirement)

**≥ 3 creational · ≥ 6 structural · ≥ 9 behavioral** (groups of 3: 18 total minimum)

NextOffer implements **22 patterns** (4 creational, 7 structural, 11 behavioral).

| Doc | Contents |
|-----|----------|
| [docs/DESIGN_PATTERNS.md](docs/DESIGN_PATTERNS.md) | Pattern catalog and class mapping |
| [docs/PLANTUML_DIAGRAMS.puml](docs/PLANTUML_DIAGRAMS.puml) | UML source |
| [docs/diagrams/png/](docs/diagrams/png/) | Exported diagrams |

---

## API reference

### Auth (`/api/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/register` | No | Create account |
| `POST` | `/login` | No | Login, returns JWT |
| `GET` | `/me` | Bearer | Current user |

Register/login body: `email`, `password` (min 8 chars), `fullName` (register only).

### Watches & jobs

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/watches` | List watches |
| `POST` | `/api/watches` | Add watch |
| `GET` | `/api/watches/{id}` | Get watch |
| `PUT` | `/api/watches/{id}` | Update watch |
| `DELETE` | `/api/watches/{id}` | Remove watch |
| `POST` | `/api/watches/{id}/poll` | Poll career page now |
| `GET` | `/api/jobs` | List jobs |
| `GET` | `/api/jobs/{id}` | Get job |

**Create watch body:** `companyName`, `careerPageUrl`, optional `boardToken`, `atsType`, `enabled`.

**`atsType`:** `GREENHOUSE`, `WORKDAY`, `SMART_RECRUITERS`

**Example URLs:**
- Greenhouse: `https://boards.greenhouse.io/stripe`
- Workday: `https://workday.wd5.myworkdayjobs.com/en-US/Workday`
- SmartRecruiters: `https://jobs.smartrecruiters.com/Visa`

Scheduled polling default: every 15 minutes (`WATCH_POLL_INTERVAL_MS`).

### Resumes & tracker

| Method | Path | Description |
|--------|------|-------------|
| `PUT` | `/api/resumes/base` | Save base resume |
| `GET` | `/api/resumes/base` | Get base resume |
| `POST` | `/api/jobs/{id}/resumes/generate` | Tailor resume for job |
| `GET` | `/api/resumes/tailored` | List tailored resumes |
| `GET` | `/api/resumes/tailored/{id}` | Get tailored resume |
| `POST` | `/api/resumes/tailored/{id}/compile-pdf` | Compile PDF |
| `GET` | `/api/resumes/tailored/{id}/download` | Download PDF or `.tex` |
| `PATCH` | `/api/jobs/{id}/status` | Update tracker status |

---

## Project structure

```
Next-Offer/
├── NextOffer/              # Spring Boot API
│   ├── mvnw, pom.xml       # Maven wrapper (no global Maven required)
│   ├── Dockerfile          # Optional Docker run
│   └── src/
│       ├── main/java/com/example/nextoffer/
│       │   ├── career/     # ATS fetch strategies, factories
│       │   ├── watch/      # Company watches, polling, observer
│       │   ├── job/        # Job postings
│       │   ├── resume/     # Tailoring, LaTeX, PDF
│       │   └── tracker/    # Application status state machine
│       ├── main/resources/ # application.properties, resume.cls, templates
│       └── test/           # JUnit tests (H2 in-memory, no Postgres needed)
├── NextOffer-FE/           # React UI
│   ├── package.json        # npm dependencies
│   └── .env.example        # Optional FE env for production API URL
├── docs/                   # DESIGN_PATTERNS.md + UML diagrams
├── scripts/
│   └── package-submission.sh
├── .env.example            # Backend env template (copy to .env)
├── GRADER_SETUP.md         # Quick guide for graders
└── README.md
```

### Important files for running

| File | Why the grader needs it |
|------|-------------------------|
| `.env.example` | Template for DB + JWT (copy to `.env`) |
| `NextOffer/mvnw` | Runs backend without installing Maven |
| `NextOffer/pom.xml` | Backend dependencies |
| `NextOffer-FE/package-lock.json` | Exact npm dependency versions |
| `docs/DESIGN_PATTERNS.md` | 22 design patterns catalog |
| `docs/diagrams/png/` | UML diagram exports |
| `GRADER_SETUP.md` | Step-by-step grader instructions |

### Create submission zip

```bash
chmod +x scripts/package-submission.sh
./scripts/package-submission.sh
```

Output: `../Next-Offer-coursework-submission.zip` (parent folder)

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Backend won't start | Check PostgreSQL is running and `SPRING_DATASOURCE_*` in `.env` |
| CORS errors in browser | Add your frontend origin to `APP_CORS_ORIGINS` |
| No PDF generated locally | Install TeX (`pdflatex`) or set `APP_RESUME_COMPILE_PDF=false` |
| No jobs after poll | Relax filters on the watch; use Settings quick-fill presets |
| Workday apply link broken | Re-poll the watch so apply URLs refresh |
