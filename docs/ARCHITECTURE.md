# OpenShelf Architecture Notes

## Design principles
- Modular monolith with clear domain packages and separation of controllers/services/repositories/entities/DTOs.
- PostgreSQL is the authoritative source of truth; Apache Solr provides full-text and faceted search.
- Indexing is recoverable: failures are tracked (`indexing_status`) and can be reconciled via reindex/retry without corrupting DB state.
- Stateless backend instances, environment-driven configuration, no committed secrets, health/readiness/metrics endpoints for operability.

## Key flows
- **Catalogue write path:** transactional DB write → Solr index update/remove → status recorded.
- **Search path:** Solr relevance/facets/filters/sort/pagination for discovery; authoritative details from DB by id.
- **XML import:** secure parse/validate/map/dedupe → preview job → commit valid records to DB + index to Solr → history/errors recorded.

## Security
- JWT authentication, BCrypt hashing, RBAC (`ADMIN`, `CATALOGUER`) enforced server-side.
- Secure XML parsing (XXE/external entities disabled), upload size limits, input validation, env-based CORS.
- Dev-only admin bootstrap configurable via environment variables (disabled by default).
