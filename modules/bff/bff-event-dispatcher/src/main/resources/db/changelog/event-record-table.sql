--liquibase formatted sql

--changeset paytogether:create-event-record-table
CREATE TABLE event_record (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    source_class VARCHAR(255) NOT NULL,
    occurred_on TIMESTAMP NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMP,
    consumed_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    consumer_handler VARCHAR(255),
    version BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE event_record;

--changeset paytogether:create-event-record-indexes
CREATE INDEX idx_event_status ON event_record(status);
CREATE INDEX idx_event_type ON event_record(event_type);
CREATE INDEX idx_source_class ON event_record(source_class);
CREATE INDEX idx_occurred_on ON event_record(occurred_on);

--rollback DROP INDEX idx_event_status;
--rollback DROP INDEX idx_event_type;
--rollback DROP INDEX idx_source_class;
--rollback DROP INDEX idx_occurred_on;

--changeset paytogether:add-event-record-comments
COMMENT ON TABLE event_record IS 'Table de stockage des événements du système avec gestion du statut de consommation';
COMMENT ON COLUMN event_record.event_id IS 'Identifiant unique de l''événement';
COMMENT ON COLUMN event_record.event_type IS 'Type de l''événement (nom de la classe)';
COMMENT ON COLUMN event_record.source_class IS 'Classe source ayant émis l''événement';
COMMENT ON COLUMN event_record.payload IS 'Données JSON de l''événement';
COMMENT ON COLUMN event_record.status IS 'Statut: PENDING, PROCESSING, CONSUMED, FAILED';
COMMENT ON COLUMN event_record.attempts IS 'Nombre de tentatives de traitement';
COMMENT ON COLUMN event_record.max_attempts IS 'Nombre maximum de tentatives autorisées';

--rollback COMMENT ON TABLE event_record IS NULL;

