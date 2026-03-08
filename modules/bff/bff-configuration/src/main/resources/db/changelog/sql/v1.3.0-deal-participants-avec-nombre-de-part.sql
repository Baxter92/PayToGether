-- =========================================================================
-- Migration: v1.3.0-deal-participants-avec-nombre-de-part
-- Date: 2026-03-08
-- Auteur: System
-- Description: Transformer la relation ManyToMany entre Deal et Utilisateur
--              en table d'association avec colonne nombre_de_part
-- =========================================================================

-- 1. Sauvegarder les données existantes de deal_participants (si la table existe)
CREATE TEMPORARY TABLE IF NOT EXISTS temp_deal_participants AS
SELECT deal_uuid, utilisateur_uuid
FROM deal_participants;

-- 2. Supprimer la table existante deal_participants
DROP TABLE IF EXISTS deal_participants CASCADE;

-- 3. Créer la nouvelle table deal_participants avec la colonne nombre_de_part
CREATE TABLE deal_participants (
    deal_uuid UUID NOT NULL,
    utilisateur_uuid UUID NOT NULL,
    nombre_de_part INTEGER NOT NULL DEFAULT 1,
    date_participation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (deal_uuid, utilisateur_uuid),

    CONSTRAINT fk_deal_participants_deal
        FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid)
        ON DELETE CASCADE,

    CONSTRAINT fk_deal_participants_utilisateur
        FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid)
        ON DELETE CASCADE,

    CONSTRAINT chk_nombre_de_part_positif
        CHECK (nombre_de_part > 0)
);

COMMENT ON TABLE deal_participants IS 'Table d''association entre Deal et Utilisateur avec nombre de parts';
COMMENT ON COLUMN deal_participants.deal_uuid IS 'UUID du deal';
COMMENT ON COLUMN deal_participants.utilisateur_uuid IS 'UUID de l''utilisateur participant';
COMMENT ON COLUMN deal_participants.nombre_de_part IS 'Nombre de parts achetées par le participant';
COMMENT ON COLUMN deal_participants.date_participation IS 'Date de participation au deal';
COMMENT ON COLUMN deal_participants.date_modification IS 'Date de dernière modification';

-- 4. Créer les index pour améliorer les performances
CREATE INDEX idx_deal_participants_deal ON deal_participants(deal_uuid);
CREATE INDEX idx_deal_participants_utilisateur ON deal_participants(utilisateur_uuid);
CREATE INDEX idx_deal_participants_date ON deal_participants(date_participation);

-- 5. Restaurer les données existantes (avec nombre_de_part = 1 par défaut)
INSERT INTO deal_participants (deal_uuid, utilisateur_uuid, nombre_de_part, date_participation, date_modification)
SELECT deal_uuid, utilisateur_uuid, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM temp_deal_participants
ON CONFLICT (deal_uuid, utilisateur_uuid) DO NOTHING;

-- 6. Supprimer la table temporaire
DROP TABLE IF EXISTS temp_deal_participants;

