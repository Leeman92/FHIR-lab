-- Reverts V2. The Idempotency-Key mechanism is dropped: no requirement asks for it, and NFR-003
-- explicitly states that creating the same patient twice need not be idempotent.
--
-- It was also unsound as built. The unique constraint covered (idempotency_key,
-- request_fingerprint), so one key could legitimately carry two fingerprints, while the lookup
-- assumed at most one row per key -- and the fingerprint was hashed from the raw request, so
-- retrying with different whitespace was reported as a conflict instead of a replay.
ALTER TABLE patients
    DROP CONSTRAINT IF EXISTS duplicate_patient;

ALTER TABLE patients
    DROP COLUMN IF EXISTS idempotency_key;

ALTER TABLE patients
    DROP COLUMN IF EXISTS request_fingerprint;
