-- =========================================================================
-- Migration: V2__ajout_table_paiement
-- Date: 2026-03-05
-- Auteur: Équipe PayToGether
-- Description: Ajout de la table Paiement et relations avec Deal/Utilisateur
-- =========================================================================

-- Table Paiement
CREATE TABLE IF NOT EXISTS paiement (
    uuid UUID PRIMARY KEY,
    deal_uuid UUID NOT NULL,
    utilisateur_uuid UUID NOT NULL,
    montant DECIMAL(10, 2) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    methode_paiement VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    date_paiement TIMESTAMP,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Relations
    CONSTRAINT fk_paiement_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE RESTRICT,
    CONSTRAINT fk_paiement_utilisateur FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE paiement IS 'Paiements effectués par les utilisateurs pour participer aux deals';
COMMENT ON COLUMN paiement.statut IS 'Statut: PENDING, COMPLETED, FAILED, REFUNDED';
COMMENT ON COLUMN paiement.methode_paiement IS 'Méthode: CREDIT_CARD, PAYPAL, SQUARE, STRIPE';
COMMENT ON COLUMN paiement.transaction_id IS 'ID de transaction fourni par le processeur de paiement';

-- Index pour performances
CREATE INDEX idx_paiement_deal_uuid ON paiement(deal_uuid);
CREATE INDEX idx_paiement_utilisateur_uuid ON paiement(utilisateur_uuid);
CREATE INDEX idx_paiement_statut ON paiement(statut);
CREATE INDEX idx_paiement_transaction_id ON paiement(transaction_id);

