# OpenShelf Troubleshooting

## Backend fails at startup: `No property 'isbn' found for type 'Resource'`

Spring Data derived-query parsing can split a camel-case property such as `isbnOrIdentifier` into an `OR` expression. `ResourceRepository.findByIsbnOrIdentifier` therefore uses an explicit JPQL `@Query`. If you add similar derived methods, either use an exact property path plus `@Query` or choose unambiguous names.

## Backend fails at startup: `Schema validation: missing table/column`

Hibernate runs with `ddl-auto: validate`, and Flyway owns the schema.

1. Confirm migrations ran: check the `flyway_schema_history` table.
2. Ensure the database user can create tables.
3. Never edit an already-applied migration; add a new one.

## Flyway: `Validate failed: Migration checksum mismatch`

An applied migration file was modified. Restore the original file, or (only in disposable local databases) drop and recreate the schema. Do not repair production blindly.

## `java.lang.ClassNotFoundException: org.eclipse.jetty.client.util.InputStreamResponseListener`

This is a SolrJ/Spring Boot Jetty version clash. OpenShelf avoids it by constructing `HttpJdkSolrClient` in `SolrConfig` rather than `Http2SolrClient`. Keep that choice unless the Jetty dependency set is aligned explicitly.

## Search returns `503 Search service is temporarily unavailable`

The backend cannot reach Solr.

1. `curl http://localhost:8983/solr/openshelf/admin/ping`
2. Check `SOLR_BASE_URL` includes `/solr` and `SOLR_COLLECTION` matches the core name.
3. Confirm the core exists: `bin/solr create -c openshelf -d solr/openshelf` (the `-d` argument is the configset parent that contains `conf/`).
4. Run the staff **Indexing → Reindex all** action or `POST /api/v1/admin/indexing/reindex`.
5. Inspect `indexing_status.last_error` for the failure reason.

## Search returns zero results after import

Solr is a derived index. Confirm:

- The import job status is `COMMITTED`/`PARTIAL` and `successfulRecords > 0`.
- `indexing_status` rows for the resources are `INDEXED`.
- The Solr core has documents: `curl 'http://localhost:8983/solr/openshelf/select?q=*:*&rows=1'`.
- Run a reindex after schema changes.

## Sorting by title fails

Title sorting uses the `title_sort` docValues field, not the tokenized `title` field. Ensure the core was created from the current `solr/openshelf/conf/managed-schema.xml` and reindex.

## `401` on staff endpoints

- Missing/expired token. Sign in again.
- `JWT_SECRET` changed, invalidating old tokens.
- Send `Authorization: Bearer <token>` exactly.
- Ensure the request goes to `/api/v1/...` and not the frontend origin without proxying.

## `403` for an ADMIN-only endpoint

The account does not have `ADMIN`. `CATALOGUER` accounts can use catalogue/import/indexing APIs but not `/api/v1/admin/users`. Role changes take effect on the next token issuance; sign in again.

## Cannot sign in with the bootstrap admin

- `BOOTSTRAP_ADMIN_ENABLED` must be exactly `true`.
- `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` must be set.
- Check backend logs for bootstrap errors.
- After provisioning, create normal accounts and disable the bootstrap admin.

## XML preview returns `400`

- Empty file, malformed XML, or DOCTYPE/external entities are rejected by design.
- The file must contain at least one `<record>` with Dublin Core fields.
- Required fields are `dc:title` and `dc:identifier`.
- Uploads larger than `MAX_UPLOAD_SIZE_MB` are rejected.

## Every imported row is reported as a duplicate

Duplicate detection is exact and case-insensitive on `dc:identifier` against `resources.isbn_or_identifier` and earlier rows in the same upload. If re-uploading the same file, expect duplicates. Change the identifier or edit/archive the existing record.

## Import committed but records are missing from search

DB commit is independent of Solr. Check `indexing_status`; failures are expected during a Solr outage and are fixed by reindex.

## Frontend cannot reach the API / CORS error

- Set `VITE_API_BASE_URL` (including `/api/v1`) and rebuild/restart the dev server.
- Add the frontend origin to `CORS_ALLOWED_ORIGINS` (comma-separated, no trailing slash).
- A `401`/`403` is not a CORS problem; check the browser network response body.

## `npm run test` fails with `React is not defined`

Vitest uses `vitest.config.ts` with `esbuild.jsx: 'automatic'`. Keep the JSX transform automatic and use the React 17+ JSX runtime; do not remove `esbuild.jsx`.

## `npm run build` compiles test files

`tsconfig.app.json` excludes `src/**/*.test.ts(x)` and `src/test`. Vite/Vitest use their own configs.

## `npm run test:e2e` fails: `libnspr4.so: cannot open shared object file`

The Playwright Chromium binary needs system libraries that minimal Linux images omit. Install them with `sudo npx playwright install-deps chromium` (or install `libnspr4` and `libnss3`). If you cannot install system packages, extract those two Debian packages into a directory and run Playwright with `LD_LIBRARY_PATH` pointing at the extracted `usr/lib/x86_64-linux-gnu` directory.

## `bin/solr create` fails to copy the configset from a mounted drive

On some WSL/DrvFs or network-mounted paths, `bin/solr create -c openshelf -d .../solr/openshelf` can fail with `File system element for parameter 'source' does not exist` or leave the core without `solrconfig.xml`. Create the core directory manually:

```bash
CORE=$SOLR_HOME/server/solr/openshelf
mkdir -p "$CORE"
cp -r /path/to/OpenShelf/solr/openshelf/conf "$CORE/conf"
printf 'name=openshelf\n' > "$CORE/core.properties"
$SOLR_HOME/bin/solr restart -p 8983
```

Verify with `curl http://localhost:8983/solr/openshelf/admin/ping`.

## PostgreSQL local verification tips

If you run PostgreSQL from an extracted package rather than a system install:

```bash
export LD_LIBRARY_PATH=/path/to/pg/usr/lib/x86_64-linux-gnu
export PATH=/path/to/pg/usr/lib/postgresql/16/bin:$PATH
initdb -D /tmp/openshelf-pg -U openshelf --pwfile=password.txt --auth=scram-sha-256
pg_ctl -D /tmp/openshelf-pg -o "-p 55432" -l /tmp/openshelf-pg.log start
createdb -h 127.0.0.1 -p 55432 -U openshelf openshelf
```

Then start the backend with matching `DB_HOST`/`DB_PORT`.
