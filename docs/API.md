# OpenShelf API

Base path: `/api/v1`. All JSON responses for application endpoints are wrapped:

```json
{
  "success": true,
  "data": { },
  "message": "Optional message",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

Errors use a flat error body:

```json
{
  "timestamp": "2026-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/admin/resources",
  "violations": [{ "field": "title", "message": "must not be blank" }]
}
```

Interactive documentation is available at `/swagger-ui.html` and `/v3/api-docs` when the backend is running.

## Authentication and roles

| Role | Access |
| --- | --- |
| Anonymous | Public catalogue, search, login, health, Swagger |
| `CATALOGUER` | Staff catalogue, imports, indexing, dashboard |
| `ADMIN` | Everything above plus `/admin/users` |

Send the JWT as `Authorization: Bearer <token>`.

### `POST /auth/login` (anonymous)

```json
{ "email": "admin@example.edu", "password": "secret" }
```

Response `data`:

```json
{
  "token": "<JWT>",
  "tokenType": "Bearer",
  "expiresInMs": 3600000,
  "user": { "id": 1, "fullName": "System Administrator", "email": "admin@example.edu", "role": "ADMIN" }
}
```

Invalid credentials return `401`. Inactive accounts are rejected with `400`.

### `GET /auth/me` (authenticated)

Returns `{ id, fullName, email, role, active }`.

## Public catalogue

### `GET /catalogue`

Database-backed browse of active resources.

| Query param | Default | Notes |
| --- | --- | --- |
| `page` | `0` | Zero-based |
| `size` | `12` | Capped at 50 |
| `sort` | `createdAt,desc` | JPA sort, e.g. `title,asc` |

Returns a Spring `Page`: `content`, `totalElements`, `totalPages`, `number`, `size`.

### `GET /catalogue/{id}`

Returns full metadata for an active resource. Increments `view_count` (used by the popular list). Archived or missing resources return `404`.

### `GET /catalogue/search`

Solr-backed search with facets. Solr must be reachable.

| Query param | Notes |
| --- | --- |
| `q` | Free text; special characters are escaped per term |
| `types` | Repeatable, e.g. `types=BOOK&types=JOURNAL` |
| `languages` | Repeatable |
| `subjects` | Repeatable |
| `yearFrom`, `yearTo` | Publication year range |
| `sort` | `title_asc`, `title_desc`, `date_asc`, `date_desc`; omitted = relevance when `q` is present, otherwise newest first |
| `page`, `size` | `0`-based; `size` capped at 50 |

Response `data`:

```json
{
  "results": [
    { "id": 1, "title": "Half of a Yellow Sun", "resourceType": "BOOK", "authors": ["Chimamanda Ngozi Adichie"], "subjects": ["African Literature"] }
  ],
  "total": 30,
  "page": 0,
  "size": 10,
  "facets": {
    "resource_type": [{ "value": "BOOK", "count": 12 }],
    "language": [{ "value": "English", "count": 30 }],
    "subjects": [{ "value": "Literature", "count": 4 }]
  }
}
```

If Solr is unavailable the endpoint returns `503` with `code: "SERVICE_UNAVAILABLE"` and `message: "Search service is temporarily unavailable"`. Catalogue reads and writes remain available because PostgreSQL is authoritative; reindex after Solr recovers.

### `GET /catalogue/filters`

Returns catalogue facet values sourced from PostgreSQL: `resourceTypes` (enum values), `languages`, `subjects`.

### `GET /catalogue/recent`

Latest 10 active resources by creation date.

### `GET /catalogue/popular`

Top 10 active resources by `view_count`, then creation date.

## Staff catalogue (`/admin`)

Requires `ADMIN` or `CATALOGUER`.

### `GET /admin/dashboard`

```json
{
  "totalResources": 31,
  "activeResources": 30,
  "archivedResources": 1,
  "indexedResources": 0,
  "pendingIndexing": 30,
  "failedIndexing": 1,
  "importJobs": 1
}
```

All values are computed from PostgreSQL; none are hardcoded.

### `GET /admin/resources?page=0&size=20`

Database-backed paged list of all resources (active and archived).

### `GET /admin/resources/{id}`

Full metadata including archived records.

### `POST /admin/resources`

```json
{
  "title": "Open Access Futures",
  "description": "…",
  "publisher": "Example Press",
  "publicationDate": "2024-01-15",
  "resourceType": "BOOK",
  "language": "English",
  "isbnOrIdentifier": "ISBN-9780000000000",
  "format": "PDF",
  "sourceUrl": "https://example.org/resource",
  "rights": "CC BY 4.0",
  "authors": ["Elena Morales"],
  "subjects": ["Open Access"]
}
```

`title` and `resourceType` are required. `resourceType` is one of `BOOK`, `RESEARCH_PAPER`, `JOURNAL`, `THESIS`, `ARTICLE`, `OTHER`. Duplicate identifiers return `409`. Authors and subjects are matched case-insensitively or created.

Response: created resource. The resource is committed to PostgreSQL first; Solr indexing is attempted best-effort and its outcome is stored in `indexing_status`.

### `PUT /admin/resources/{id}`

Same body as create. Returns the updated resource.

### `DELETE /admin/resources/{id}`

Archives the resource (soft delete) and removes it from the search index best-effort. Returns `data: null` and `message: "Resource archived"`.

## Dublin Core XML import

Requires `ADMIN` or `CATALOGUER`. Securely parsed: DTDs/DOCTYPE, external entities (XXE), and external schema access are disabled; uploads are limited by `MAX_UPLOAD_SIZE_MB` (default 10 MB).

### `POST /admin/imports/preview`

`multipart/form-data` with part `file` (`.xml`).

Parses the whole file, validates required `dc:title` and `dc:identifier`, detects duplicate identifiers against both the catalogue and earlier records in the same upload, persists a preview job, and stores the valid payload for commit.

```json
{
  "importJobId": 12,
  "filename": "dublin-core-valid.xml",
  "totalRecords": 2,
  "validRecords": 2,
  "invalidRecords": 0,
  "duplicateRecords": 0,
  "records": [
    {
      "identifier": "DOI-10.1000/osw.2023.010",
      "title": "Open Scholarship Workflows",
      "creator": "Elena Morales",
      "date": "2023-05-10",
      "type": "Research Paper",
      "errors": [],
      "duplicate": false
    }
  ]
}
```

Categories are mutually exclusive: a record is counted as valid, duplicate, or invalid.

### `POST /admin/imports/{id}/commit`

Commits only the valid preview rows. Duplicate rows are skipped and counted. Solr indexing is attempted best-effort; DB records are not rolled back if indexing fails. Repeating a commit on a `COMMITTED` or `PARTIAL` job is idempotent.

```json
{ "importJobId": 12, "status": "COMMITTED", "successfulRecords": 2, "failedRecords": 0, "duplicateRecords": 0 }
```

### `GET /admin/imports`

Import history, newest first, with record-level errors.

### `GET /admin/imports/{id}`

Job detail: counts, importer, timestamps, and errors.

## Indexing

Requires `ADMIN` or `CATALOGUER`.

### `GET /admin/indexing/status`

```json
{
  "indexed": 31,
  "pending": 0,
  "failed": 0,
  "failures": [
    { "resourceId": 31, "lastIndexedAt": null, "lastError": "ConnectException (could not reach Apache Solr; check SOLR_BASE_URL, SOLR_COLLECTION and that the core is running)" }
  ]
}
```

`pending` includes active/archived resources that do not yet have an `INDEXED` status row.

### `POST /admin/indexing/reindex`

Rebuilds the index for every resource from PostgreSQL. Archived resources are deleted from Solr. Returns `{ "indexed": 31, "failed": 0 }`. This is the supported recovery path after Solr outages or schema changes.

## User management (ADMIN only)

### `GET /admin/users`

Returns `[{ id, fullName, email, role, active }]`.

### `POST /admin/users`

```json
{ "fullName": "Jane Cataloguer", "email": "jane@example.edu", "password": "strong-password", "role": "CATALOGUER", "active": true }
```

Password is BCrypt-hashed. Duplicate emails return `409`.

### `PUT /admin/users/{id}`

`{ fullName, email, password?, role, active }`. Omit or blank `password` to keep the existing one.

### `DELETE /admin/users/{id}`

Deletes the account. The last active admin cannot be removed by the current implementation guard (returns `409`).

### `GET /admin/system/health`

Admin-scoped health summary: `{ status, application, profiles, timestamp }`. Public probes remain at `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness`.

## Operational endpoints

| Endpoint | Auth | Purpose |
| --- | --- | --- |
| `/actuator/health` | Public (details hidden) | Overall health |
| `/actuator/health/liveness` | Public | Liveness probe |
| `/actuator/health/readiness` | Public | Readiness probe |
| `/actuator/info` | Public | Build/app info |
| `/actuator/metrics/**` | Public | Micrometer metrics |
| `/actuator/prometheus` | Public | Prometheus scrape |
| `/swagger-ui.html` | Public | Swagger UI |
| `/v3/api-docs` | Public | OpenAPI document |
