-- =========================================================================
-- Template pour nouvelle migration SQL
-- Version: V{MAJOR}.{MINOR}.{PATCH}__{description_en_snake_case}.sql
-- Date: YYYY-MM-DD
-- Auteur: Ton nom
-- Description: Description détaillée de la migration
-- =========================================================================

-- Exemple 1: Ajouter une colonne
-- ALTER TABLE nom_table
-- ADD COLUMN IF NOT EXISTS nouvelle_colonne VARCHAR(100);

-- Exemple 2: Modifier une colonne
-- ALTER TABLE nom_table
-- ALTER COLUMN colonne_existante TYPE VARCHAR(255);

-- Exemple 3: Ajouter une clé étrangère
-- ALTER TABLE table_source
-- ADD CONSTRAINT fk_nom_relation FOREIGN KEY (colonne_uuid)
--     REFERENCES table_cible(uuid) ON DELETE CASCADE;

-- Exemple 4: Supprimer une contrainte
-- ALTER TABLE nom_table
-- DROP CONSTRAINT IF EXISTS fk_ancienne_relation;

-- Exemple 5: Créer un index
-- CREATE INDEX IF NOT EXISTS idx_nom_table_colonne ON nom_table(colonne);

-- Exemple 6: Ajouter un commentaire
-- COMMENT ON TABLE nom_table IS 'Description de la table';
-- COMMENT ON COLUMN nom_table.colonne IS 'Description de la colonne';

-- =========================================================================
-- Notes importantes:
-- - Toujours utiliser IF NOT EXISTS / IF EXISTS pour l'idempotence
-- - Ajouter des commentaires pour la documentation
-- - Créer des index pour les colonnes fréquemment recherchées
-- - Définir les contraintes ON DELETE (CASCADE, RESTRICT, SET NULL)
-- =========================================================================

