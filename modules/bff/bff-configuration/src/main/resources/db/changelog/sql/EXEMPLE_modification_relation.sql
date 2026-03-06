-- =========================================================================
-- Version: 1.3.0
-- Date: 2026-03-05
-- Auteur: Équipe PayToGether
-- Description: EXEMPLE - Modification d'une relation existante
--              Renommage de la colonne createur_uuid en proprietaire_uuid
-- =========================================================================

-- ÉTAPE 1: Supprimer la contrainte de clé étrangère existante
ALTER TABLE deal
DROP CONSTRAINT IF EXISTS fk_deal_createur;

-- ÉTAPE 2: Renommer la colonne
ALTER TABLE deal
RENAME COLUMN createur_uuid TO proprietaire_uuid;

-- ÉTAPE 3: Recréer la contrainte de clé étrangère avec le nouveau nom
ALTER TABLE deal
ADD CONSTRAINT fk_deal_proprietaire
    FOREIGN KEY (proprietaire_uuid)
    REFERENCES utilisateur(uuid)
    ON DELETE CASCADE;

-- ÉTAPE 4: Mettre à jour les commentaires
COMMENT ON COLUMN deal.proprietaire_uuid IS 'UUID du propriétaire du deal (anciennement createur_uuid)';

-- ÉTAPE 5: Supprimer l'ancien index et en créer un nouveau
DROP INDEX IF EXISTS idx_deal_createur_uuid;
CREATE INDEX idx_deal_proprietaire_uuid ON deal(proprietaire_uuid);

-- =========================================================================
-- Notes:
-- Ce script est un EXEMPLE pour montrer comment modifier une relation.
-- NE PAS l'appliquer si tu veux garder le nom createur_uuid !
-- =========================================================================

