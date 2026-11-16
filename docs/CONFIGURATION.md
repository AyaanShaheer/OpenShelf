# OpenShelf Configuration Reference

All runtime configuration is environment-driven. Defaults are safe for local development only; production values must be supplied by the deployment platform. No real secrets belong in this repository.

## Backend environment variables

| Variable | Default | Required in production | Description |
| --- | --- | --- | --- |
| `SERVER_PORT` | `8080` | No | HTTP port |
| `SPRING_PROFILES_ACTIVE` | `dev` | No | Spring profile; use a non-dev profile in production |
| `DB_HOST` | `localhost` | Yes | PostgreSQL host |
| `DB_PORT` | `5432` | Yes | PostgreSQL port |
| `DB_NAME` | `openshelf` | Yes | Database name |
| `DB_USER` | `openshelf` | Yes | Database user |
| `DB_PASSWORD` | `change-me` | Yes | Database password |
| `SOLR_BASE_URL` | `http://localhost:8983/solr` | Yes | Solr base URL, without the collection name |
| `SOLR_COLLECTION` | `openshelf` | Yes | Solr collection/core name |
| `JWT_SECRET` | dev-only placeholder | Yes | HMAC signing secret; at least 32 bytes, random, stored as a secret |
| `JWT_EXPIRATION_MS` | `3600000` | No | Token lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Yes | Comma-separated allowed browser origins |
| `MAX_UPLOAD_SIZE_MB` | `10` | No | Max XML upload size per file/request |
| `BOOTSTRAP_ADMIN_ENABLED` | `false` | No | Creates/updates the bootstrap admin on startup when `true` |
| `BOOTSTRAP_ADMIN_EMAIL` | empty | Only if bootstrap enabled | Bootstrap admin email |
| `BOOTSTRAP_ADMIN_PASSWORD` | empty | Only if bootstrap enabled | Bootstrap admin password (never commit) |
| `BOOTSTRAP_ADMIN_FULL_NAME` | `System Administrator` | No | Bootstrap admin display name |

Actuator management endpoints use the defaults exposed by `management.endpoints.web.exposure.include`: `health,info,prometheus,metrics`.

## Frontend environment variables

Vite exposes only variables prefixed with `VITE_` to the browser.

| Variable | Default | Description |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Backend API base URL, including `/api/v1` |

The frontend stores the JWT in `localStorage` under `openshelf_token`. Do not put secrets in any `VITE_` variable; browser-exposed values are public.

## PostgreSQL

- Version: PostgreSQL 15 or newer (validated against PostgreSQL 16.15).
- Migrations are managed by Flyway at application startup from `backend/src/main/resources/db/migration`.
- Never edit an applied migration; Flyway validates checksums and will refuse to start on drift. Add a new `V{n}__description.sql` instead.
- Seed data comes from `V2__seed_data.sql` (20 authors, 15 subjects, 30 resources).

Minimal local setup:

```bash
createuser --pwprompt openshelf
createdb --owner=openshelf openshelf
export DB_HOST=localhost DB_PORT=5432 DB_NAME=openshelf DB_USER=openshelf DB_PASSWORD='...'
```

## Apache Solr

- Version: Solr 9.7 (configset validated against 9.7.0 / Lucene 9.11).
- Collection/core name must match `SOLR_COLLECTION`.
- The schema lives in `solr/openshelf/conf/`. `title_sort` is a docValues string field used for title sorting; `_text_` is the default search field populated by copy fields.
- After changing the schema, restart/reload the core and run `POST /api/v1/admin/indexing/reindex`.
- Solr is a derived index. PostgreSQL is authoritative. Losing or corrupting the index must be recoverable via reindex.

Standalone core setup:

```bash
SOLR_HOME=/opt/solr
$SOLR_HOME/bin/solr start -p 8983
$SOLR_HOME/bin/solr create -c openshelf -d /path/to/OpenShelf/solr/openshelf
curl http://localhost:8983/solr/openshelf/admin/ping
```

The backend uses SolrJ's JDK HTTP client (`HttpJdkSolrClient`), so no Jetty client compatibility shim is required.

## Security notes

- RBAC is enforced server-side in `SecurityConfig`; the frontend role checks are only UX.
- XML parsing disables DOCTYPE/DTD and external entities, and uploads are size-limited.
- Swagger and health/metrics are public by default. If production policy requires them private, change `SecurityConfig` and the actuator exposure settings.
- Rotate `JWT_SECRET` to invalidate all existing tokens.
- The bootstrap admin is disabled by default and should be disabled again after initial provisioning.
