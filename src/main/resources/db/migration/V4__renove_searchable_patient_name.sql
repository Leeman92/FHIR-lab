DROP INDEX IF EXISTS idx_patients_searchable_name_trgm;

ALTER TABLE patients
    DROP COLUMN IF EXISTS searchable_name;