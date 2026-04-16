-- =========================================================================
-- Migration: V8__ajout_index_performance_deal
-- Date: 2026-04-16
-- Auteur: Système d'optimisation
-- Description: Ajout d'index pour améliorer les performances des requêtes
--              sur la table deal
-- =========================================================================

-- ============================================================
-- INDEX POUR AMÉLIORER LES PERFORMANCES DES REQUÊTES
-- ============================================================

-- Index sur le statut (requêtes fréquentes findByStatut)
CREATE INDEX IF NOT EXISTS idx_deal_statut
ON deal(statut);

COMMENT ON INDEX idx_deal_statut IS 'Index pour optimiser les requêtes par statut';

-- Index sur le créateur/marchand UUID (requêtes fréquentes findByMarchandJpa)
CREATE INDEX IF NOT EXISTS idx_deal_marchand_uuid
ON deal(marchand_uuid);

COMMENT ON INDEX idx_deal_marchand_uuid IS 'Index pour optimiser les requêtes par créateur';

-- Index sur la catégorie UUID (requêtes fréquentes findByCategorieJpa)
CREATE INDEX IF NOT EXISTS idx_deal_categorie_uuid
ON deal(categorie_uuid);

COMMENT ON INDEX idx_deal_categorie_uuid IS 'Index pour optimiser les requêtes par catégorie';

-- Index composite sur statut + favoris + date_creation
-- (optimise la requête findByStatutOrderByFavorisDescDateCreationDesc)
CREATE INDEX IF NOT EXISTS idx_deal_statut_favoris_date_creation
ON deal(statut, favoris DESC, date_creation DESC);

COMMENT ON INDEX idx_deal_statut_favoris_date_creation IS 'Index composite pour les tris par statut, favoris et date de création';

-- Index composite sur favoris + date_creation
-- (optimise la requête findAllByOrderByFavorisDescDateCreationDesc)
CREATE INDEX IF NOT EXISTS idx_deal_favoris_date_creation
ON deal(favoris DESC, date_creation DESC);

COMMENT ON INDEX idx_deal_favoris_date_creation IS 'Index composite pour les tris par favoris et date de création';

-- Index sur la date d'expiration pour identifier rapidement les deals expirés
CREATE INDEX IF NOT EXISTS idx_deal_date_expiration
ON deal(date_expiration);

COMMENT ON INDEX idx_deal_date_expiration IS 'Index pour identifier rapidement les deals expirés';

-- Index composite pour optimiser les recherches combinées statut + date expiration
CREATE INDEX IF NOT EXISTS idx_deal_statut_date_expiration
ON deal(statut, date_expiration);

COMMENT ON INDEX idx_deal_statut_date_expiration IS 'Index pour optimiser la vérification des deals publiés expirés';

-- Index sur ville pour les recherches géographiques
CREATE INDEX IF NOT EXISTS idx_deal_ville
ON deal(ville);

COMMENT ON INDEX idx_deal_ville IS 'Index pour optimiser les recherches par ville';

-- Index sur date_creation seul (pour tri descendant)
CREATE INDEX IF NOT EXISTS idx_deal_date_creation
ON deal(date_creation DESC);

COMMENT ON INDEX idx_deal_date_creation IS 'Index pour optimiser les tris par date de création';

-- ============================================================
-- ANALYSE DES INDEX (pour mettre à jour les statistiques)
-- ============================================================

ANALYZE deal;

