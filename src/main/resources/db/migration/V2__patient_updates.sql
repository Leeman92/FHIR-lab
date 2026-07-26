ALTER TABLE
    patients
ADD COLUMN
    idempotency_key VARCHAR(255) NOT NULL;

ALTER TABLE
    patients
ADD COLUMN
    request_fingerprint VARCHAR(64) NOT NULL;

ALTER TABLE
    patients
ADD CONSTRAINT duplicate_patient UNIQUE (idempotency_key, request_fingerprint);