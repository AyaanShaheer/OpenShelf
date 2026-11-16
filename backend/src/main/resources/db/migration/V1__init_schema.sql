CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'CATALOGUER'))
);

CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    biography TEXT
);

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    CONSTRAINT uq_subject_name_parent UNIQUE (name, parent_subject_id)
);

CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    publisher VARCHAR(255),
    publication_date DATE,
    resource_type VARCHAR(50) NOT NULL,
    language VARCHAR(100),
    isbn_or_identifier VARCHAR(255),
    format VARCHAR(100),
    source_url TEXT,
    rights TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_resources_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_resources_identifier_not_null ON resources (isbn_or_identifier) WHERE isbn_or_identifier IS NOT NULL;
CREATE INDEX idx_resources_title ON resources (title);
CREATE INDEX idx_resources_type ON resources (resource_type);
CREATE INDEX idx_resources_language ON resources (language);
CREATE INDEX idx_resources_publication_date ON resources (publication_date);
CREATE INDEX idx_resources_status ON resources (status);

CREATE TABLE resource_authors (
    resource_id BIGINT NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (resource_id, author_id)
);

CREATE TABLE resource_subjects (
    resource_id BIGINT NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    PRIMARY KEY (resource_id, subject_id)
);

CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_records INT NOT NULL DEFAULT 0,
    successful_records INT NOT NULL DEFAULT 0,
    failed_records INT NOT NULL DEFAULT 0,
    duplicate_records INT NOT NULL DEFAULT 0,
    imported_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    preview_payload TEXT,
    CONSTRAINT chk_import_jobs_status CHECK (status IN ('PREVIEW', 'COMMITTED', 'FAILED', 'PARTIAL'))
);

CREATE TABLE import_errors (
    id BIGSERIAL PRIMARY KEY,
    import_job_id BIGINT NOT NULL REFERENCES import_jobs(id) ON DELETE CASCADE,
    record_identifier VARCHAR(255),
    error_message TEXT NOT NULL
);

CREATE INDEX idx_import_errors_job_id ON import_errors(import_job_id);

CREATE TABLE indexing_status (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL UNIQUE REFERENCES resources(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    last_indexed_at TIMESTAMP WITH TIME ZONE,
    last_error TEXT,
    CONSTRAINT chk_indexing_status CHECK (status IN ('PENDING', 'INDEXED', 'FAILED'))
);

CREATE INDEX idx_indexing_status_status ON indexing_status(status);
CREATE INDEX idx_authors_full_name ON authors(full_name);
CREATE INDEX idx_subjects_name ON subjects(name);
