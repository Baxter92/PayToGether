-- =========================================================================
-- Migration: V6__ajout_colonne_favoris_deal
-- Date: 2026-04-08
-- Auteur: PayToGether Team
-- Description: Ajout de la colonne favoris dans la table deal
--              Permet aux administrateurs de marquer des deals comme favoris
-- =========================================================================

-- Ajout de la colonne favoris (boolean, par défaut false)
ALTER TABLE deal ADD COLUMN favoris BOOLEAN NOT NULL DEFAULT false;

-- Commentaire sur la colonne
COMMENT ON COLUMN deal.favoris IS 'Indique si le deal est marqué comme favori par les administrateurs';

-- Index pour optimiser les requêtes sur les deals favoris
CREATE INDEX idx_deal_favoris ON deal(favoris) WHERE favoris = true;

-- Log de la migration
DO $$
BEGIN
    RAISE NOTICE '✅ Colonne favoris ajoutée à la table deal';
END $$;

