-- FR-003 searches names by containment: lower(<name>) LIKE '%term%'.
-- No btree index can serve a leading-wildcard pattern, but pg_trgm's GIN operator class can.
-- Indexing the lower-cased expressions matches the generated predicate exactly.
--
-- Terms shorter than three characters carry no full trigram and still fall back to a sequential
-- scan; that is acceptable for the data volumes this project targets.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_patients_given_name_trgm
    ON patients USING gin (lower(given_name) gin_trgm_ops);

CREATE INDEX idx_patients_family_name_trgm
    ON patients USING gin (lower(family_name) gin_trgm_ops);
