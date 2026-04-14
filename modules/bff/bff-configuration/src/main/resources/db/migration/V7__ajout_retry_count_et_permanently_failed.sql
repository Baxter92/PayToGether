-- =========================================================================
-- Migration: V7__ajout_retry_count_et_permanently_failed
-- Date: 2026-04-15
-- Auteur: GitHub Copilot
-- Description: Ajout du champ retry_count et du statut PERMANENTLY_FAILED
--              pour les événements qui échouent après retraitement manuel
-- =========================================================================

-- Ajouter la colonne retry_count pour tracker les retraitements manuels
ALTER TABLE event_record ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN event_record.retry_count IS 'Nombre de retraitements manuels de l''événement';

-- Créer un index sur retry_count pour les requêtes de statistiques
CREATE INDEX IF NOT EXISTS idx_event_retry_count ON event_record(retry_count);

-- Note: Le statut PERMANENTLY_FAILED est géré automatiquement par l'enum Java EventStatus
-- Pas besoin de modification de la colonne status (VARCHAR)

-- Logs
SELECT 'Migration V7 appliquée avec succès' AS message;

