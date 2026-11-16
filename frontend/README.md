# OpenShelf Frontend

React 19 + TypeScript + Vite single-page application for the OpenShelf public discovery portal and staff back office.

## Scripts

```bash
npm install
npm run dev        # Vite dev server on http://localhost:5173
npm run build      # TypeScript project build + production bundle in dist/
npm run preview    # serve the production build
npm run test       # Vitest unit/component tests
npm run test:e2e   # Playwright smoke tests (starts the dev server automatically)
npm run lint       # Oxlint
```

## Configuration

Only `VITE_`-prefixed variables reach the browser. The only required value is the backend API base URL:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

See `../docs/CONFIGURATION.md` for the full backend configuration reference.

## Structure

```text
src/
  api/          Typed fetch client and API functions
  assets/       Static assets
  components/   Shared UI and route guards
  contexts/     Authentication state
  layouts/      Public and staff shells
  pages/
    public/     Home, search, resource detail, login
    staff/      Dashboard, catalogue, imports, indexing, users
  test/         Vitest setup and fixtures
  types/        Shared TypeScript types
e2e/            Playwright smoke specs
```

## Testing notes

- Unit/component tests use Vitest + React Testing Library with `esbuild.jsx: 'automatic'`.
- Playwright starts `npm run dev` on `127.0.0.1:5173` unless `E2E_BASE_URL` is set.
- The E2E suite needs a running backend (and Solr for search-related flows). Point the dev server at it with `VITE_API_BASE_URL`.
- Minimal Linux images may need `libnspr4`/`libnss3` for Chromium; run `npx playwright install-deps chromium` where package installation is permitted.
