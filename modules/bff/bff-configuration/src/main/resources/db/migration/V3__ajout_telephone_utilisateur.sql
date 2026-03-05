-- =========================================================================
-- Migration: V3__ajout_telephone_utilisateur
-- Date: 2026-03-05
-- Auteur: Équipe PayToGether
-- Description: Ajout des colonnes téléphone et statut_telephone pour les utilisateurs
-- =========================================================================

-- Ajout de la colonne téléphone
ALTER TABLE utilisateur
ADD COLUMN IF NOT EXISTS telephone VARCHAR(20);

-- Ajout de la colonne statut_telephone
ALTER TABLE utilisateur
ADD COLUMN IF NOT EXISTS statut_telephone VARCHAR(20) DEFAULT 'NON_VERIFIE';

-- Commentaires
COMMENT ON COLUMN utilisateur.telephone IS 'Numéro de téléphone de l''utilisateur (format international)';
COMMENT ON COLUMN utilisateur.statut_telephone IS 'Statut: NON_VERIFIE, EN_ATTENTE, VERIFIE';

-- Index pour recherche par téléphone
CREATE INDEX IF NOT EXISTS idx_utilisateur_telephone ON utilisateur(telephone);

