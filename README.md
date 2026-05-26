# Next-Offer

AI-powered job application assistant for students: watch target company career pages, detect new SWE roles, and generate tailored ATS-friendly resumes.

## Repositories

| Module | Stack |
|--------|--------|
| [NextOffer](NextOffer/) | Spring Boot 4, PostgreSQL, JPA |
| [NextOffer-FE](NextOffer-FE/) | React 19, TypeScript, Vite |

## Design patterns (course requirement)

**≥ 3 creational · ≥ 6 structural · ≥ 9 behavioral**

See **[docs/DESIGN_PATTERNS.md](docs/DESIGN_PATTERNS.md)** for the full catalog and class-level mapping. Code modules: `career`, `watch`, `job`, `resume`, `tracker` under `com.example.nextoffer`.

## Local setup

```bash
cp .env.example .env
# Set SPRING_DATASOURCE_* and JWT_SECRET (min 32 chars) in .env at repo root

cd NextOffer && ./mvnw spring-boot:run
cd NextOffer-FE && npm install && npm run dev
```

## Auth API (`/api/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/register` | No | Create account |
| `POST` | `/api/auth/login` | No | Login, returns JWT |
| `GET` | `/api/auth/me` | Bearer token | Current user profile |

Register/login body (JSON): `email`, `password` (min 8 chars), `fullName` (register only).  
Response: `{ "token": "...", "user": { "id", "email", "fullName", "createdAt" } }`.
