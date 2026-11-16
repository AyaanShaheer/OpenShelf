ALTER TABLE resources ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_resources_view_count ON resources (view_count DESC);
