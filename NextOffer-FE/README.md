# NextOffer Frontend

React + TypeScript frontend for [NextOffer](../NextOffer) (Spring Boot API).

## Stack

- [React 19](https://react.dev/)
- [TypeScript](https://www.typescriptlang.org/)
- [Vite](https://vite.dev/)

## Getting started

```bash
npm install
npm run dev
```

App runs at http://localhost:5173. API requests to `/api/*` are proxied to the backend at http://localhost:8080 during development (start the Spring Boot app first).

## Scripts

| Command | Description |
| --- | --- |
| `npm run dev` | Start dev server with HMR |
| `npm run build` | Production build to `dist/` |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |
