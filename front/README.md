# PropFlow frontend

Angular 17 single-page application for the PropFlow (Proposal Admin) demo: proposals, candidates, positions, offices, taxes, extras, parameters and a dashboard. It is a standalone-component app using PrimeNG, Tailwind CSS and ngx-translate (English, Spanish, Portuguese and German), and it reads and writes through the backend REST API.

## Requirements

- Node.js 20 and npm
- Google Chrome, for the Karma tests
- A running backend on <http://localhost:8080> (the API base URL is set in `src/environments/`)

## Run

```bash
npm ci
npm start
```

The dev server is at <http://localhost:4200>. From the repository root, `./dev.sh` starts the backend, the frontend and the supporting Docker services together. The login form accepts any non-empty username and password.

## Test, lint and build

```bash
npm test              # Karma/Jasmine in a visible Chrome, watch mode
npm run test:ci       # headless Chrome with coverage, as CI runs it
npm run lint          # ESLint for TypeScript and templates
npm run format:check  # Prettier check
npm run format        # Prettier write
npm run build         # production build in dist/front/browser
```

## Folder overview

- `src/app/pages/` — routed screens: dashboard, proposals, new and detailed proposal, candidates, positions, taxes, extras and parameters.
- `src/app/components/` — reusable UI pieces such as the navbar, login form and new/duplicate dialogs and buttons.
- `src/app/services/` — HTTP services for each API area, plus the error toast interceptor and toast service.
- `src/app/utils/` — shared TypeScript types and helper functions.
- `src/assets/i18n/` — translation files (`en.json`, `es.json`, `pt.json`, `de.json`).
- `src/environments/` — API endpoint for the development and production builds.
- `src/testing/` — shared Jasmine fixtures and test providers.
