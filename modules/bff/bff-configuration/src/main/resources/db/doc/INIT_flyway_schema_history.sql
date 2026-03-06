-- =========================================================================
-- Script manuel : Initialisation de flyway_schema_history
-- Date: 2026-03-06
-- Auteur: Équipe PayToGether
-- Description: Marque les migrations V1, V2, V3, V4 comme déjà appliquées
-- =========================================================================
--
-- ⚠️ EXÉCUTER CE SCRIPT UNE SEULE FOIS
-- Ce script permet de démarrer Flyway sur une base existante créée par Hibernate
--
-- =========================================================================

-- Créer la table flyway_schema_history si elle n'existe pas
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL PRIMARY KEY,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL
);

-- Marquer V1, V2, V3, V4 comme déjà appliquées
-- Cela évite que Flyway essaie de les réexécuter sur la base existante
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES
    (1, '1', 'schema initial', 'SQL', 'V1__schema_initial.sql', 0, 'manual', 0, true),
    (2, '2', 'ajout table paiement', 'SQL', 'V2__ajout_table_paiement.sql', 0, 'manual', 0, true),
    (3, '3', 'ajout telephone utilisateur', 'SQL', 'V3__ajout_telephone_utilisateur.sql', 0, 'manual', 0, true),
    (4, '4', 'ajout tables manquantes', 'SQL', 'V4__ajout_tables_manquantes.sql', 0, 'manual', 0, true)
ON CONFLICT (installed_rank) DO NOTHING;

-- Vérifier que les migrations ont été enregistrées
SELECT installed_rank, version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;

-- ✅ Résultat attendu : 4 lignes (V1, V2, V3, V4) marquées comme appliquées
-- Après avoir exécuté ce script :
-- 1. Réactiver Flyway dans application.properties
-- 2. Redémarrer l'application
-- 3. Les nouvelles migrations (V5+) seront automatiquement appliquées

