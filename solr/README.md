# Solr configuration (OpenShelf)

OpenShelf uses Apache Solr for full-text and faceted search. PostgreSQL remains the authoritative source of truth.

## Collection/core
- Default collection name: `openshelf` (configurable via `SOLR_COLLECTION`)
- Base URL: `SOLR_BASE_URL` (e.g. `http://localhost:8983/solr`)

## Schema
The provided configuration in `solr/openshelf/conf/` includes:
- `managed-schema.xml` with fields for `id`, `title`, `description`, `publisher`, `authors`, `subjects`, `resource_type`, `language`, `publication_year`, `publication_date`, `isbn_or_identifier`, `status`, timestamps, and `_text_` copy fields.
- `title_sort` (single-valued docValues string) for deterministic title sorting and `subject_facet` (multi-valued docValues string) for full-name subject facets and filters, both populated by copy fields.
- `solrconfig.xml` with basic `/select` and `/update` handlers.
- `stopwords.txt`.

## Local setup (example)
1. Start Solr and create a collection/core named `openshelf`.
2. Apply the provided configset (or copy `conf/` files) to the collection as appropriate for your Solr deployment mode.
3. Start the backend with `SOLR_BASE_URL` and `SOLR_COLLECTION` set.
4. Trigger an initial re-index from the staff portal or `POST /api/v1/admin/indexing/reindex` to populate the search index from seeded PostgreSQL data.

If `bin/solr create` cannot read the configset from a mounted drive, copy the files manually and restart Solr:

```bash
CORE=$SOLR_HOME/server/solr/openshelf
mkdir -p "$CORE"
cp -r /path/to/OpenShelf/solr/openshelf/conf "$CORE/conf"
printf 'name=openshelf\n' > "$CORE/core.properties"
$SOLR_HOME/bin/solr restart -p 8983
```

## Notes
- Only `ACTIVE` resources are indexed for public search; archived resources are removed from the index.
- Indexing failures are tracked in PostgreSQL (`indexing_status`) and can be recovered via re-index/retry operations.
