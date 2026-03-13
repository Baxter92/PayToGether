-- =========================================================================
-- Migration: V5__ajout_flux_payout_factures
-- Date: 2026-03-12
-- Auteur: PayToGether Team
-- Description: Ajout des colonnes pour gérer le flux de payout et facturation
-- =========================================================================

-- Ajout des colonnes dans la table commande
ALTER TABLE commande
    ADD COLUMN date_depot_payout TIMESTAMP,
    ADD COLUMN facture_marchand_url VARCHAR(500);

COMMENT ON COLUMN commande.date_depot_payout IS 'Date de dépôt du payout par l''admin';
COMMENT ON COLUMN commande.facture_marchand_url IS 'URL de la facture du marchand dans MinIO';

-- Création d'index pour améliorer les performances
CREATE INDEX idx_commande_date_depot_payout ON commande(date_depot_payout);
CREATE INDEX idx_commande_facture_marchand_url ON commande(facture_marchand_url);

-- Note: La table commande_utilisateur existe déjà avec la colonne statut_commande_utilisateur
-- Aucune modification nécessaire pour cette table

