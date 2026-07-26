CREATE TABLE patients
(
    id          uuid          NOT NULL,
    active      boolean       NOT NULL,
    given_name  varchar(100)  NOT NULL,
    family_name varchar(100)  NOT NULL,
    birth_date  date          NOT NULL,
    created_at  timestamptz   NOT NULL,
    updated_at  timestamptz   NOT NULL,
    CONSTRAINT pk_patient PRIMARY KEY (id)
);

CREATE TABLE practitioners
(
    id          uuid          NOT NULL,
    active      boolean       NOT NULL,
    display_name varchar(200) NOT NULL,
    speciality  varchar(200)  NOT NULL,
    created_at  timestamptz   NOT NULL,
    updated_at  timestamptz   NOT NULL,
    CONSTRAINT pk_practitioners PRIMARY KEY (id)
);

CREATE TABLE rooms
(
    id           uuid         NOT NULL,
    active       boolean      NOT NULL,
    display_name varchar(200) NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    CONSTRAINT pk_rooms PRIMARY KEY (id),
    CONSTRAINT uq_rooms_display_name UNIQUE (display_name)
);

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE appointments
(
    id              uuid        NOT NULL,
    patient_id      uuid        NOT NULL,
    practitioner_id uuid        NOT NULL,
    room_id         uuid        NOT NULL,

    start_time      timestamptz NOT NULL,
    end_time        timestamptz NOT NULL,

    status          varchar(20) NOT NULL,
    reason          varchar(500),

    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,

    CONSTRAINT pk_appointments PRIMARY KEY (id),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appointments_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioners (id),
    CONSTRAINT fk_appointments_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_appointments_status CHECK ( status IN ('BOOKED', 'CANCELLED', 'COMPLETED', 'NO_SHOW') ),
    CONSTRAINT chk_appointments_time_order CHECK ( end_time > start_time ),
    CONSTRAINT chk_appointments_start_five_minutes CHECK ( mod(EXTRACT(EPOCH FROM start_time), 300) = 0 ),
    CONSTRAINT chk_appointments_end_five_minutes CHECK ( mod(EXTRACT(EPOCH FROM end_time), 300) = 0 ),
    CONSTRAINT chk_appointments_duration CHECK ( end_time - start_time BETWEEN INTERVAL '15 minutes' AND INTERVAL '3 hours'),

    CONSTRAINT ex_appointments_no_room_overlap EXCLUDE USING gist (
        room_id WITH =,
        (tstzrange(start_time, end_time, '[)')) WITH &&
    ) WHERE (status <> 'CANCELLED'),

    CONSTRAINT ex_appointments_no_practitioner_overlap EXCLUDE USING gist (
        practitioner_id WITH =,
        (tstzrange(start_time, end_time, '[)')) WITH &&
    ) WHERE (status <> 'CANCELLED'),

    CONSTRAINT ex_appointments_no_patient_overlap EXCLUDE USING gist (
        patient_id WITH =,
        (tstzrange(start_time, end_time, '[)')) WITH &&
    ) WHERE (status <> 'CANCELLED')
);

