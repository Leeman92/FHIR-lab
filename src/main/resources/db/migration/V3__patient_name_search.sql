CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE
    patients
ADD COLUMN
    searchable_name text GENERATED ALWAYS AS (
        lower(
            coalesce(given_name, '')
                || ' '
                || coalesce(family_name, '')
        )
    ) STORED;

CREATE INDEX idx_patients_searchable_name_trgm ON patients USING gin (searchable_name gin_trgm_ops);