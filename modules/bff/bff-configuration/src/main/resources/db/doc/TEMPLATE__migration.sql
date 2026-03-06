-- =========================================================================
-- TEMPLATE POUR NOUVELLE MIGRATION FLYWAY
-- Copier ce fichier et renommer: V{N}__{description_en_snake_case}.sql
-- =========================================================================

-- =========================================================================
-- Migration: V{N}__{description}
-- Date: YYYY-MM-DD
-- Auteur: Ton nom
-- Description: Description détaillée de ce que fait la migration
-- =========================================================================

-- Exemples de cas d'usage courants:

-- 1. CRÉER UNE TABLE
-- CREATE TABLE IF NOT EXISTS ma_table (
--     uuid UUID PRIMARY KEY,
--     nom VARCHAR(100) NOT NULL,
--     date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
--     date_modification TIMESTAMP NOT NULL DEFAULT NOW(),
--
--     CONSTRAINT fk_ma_table_autre FOREIGN KEY (autre_uuid)
--         REFERENCES autre_table(uuid) ON DELETE CASCADE
-- );
--
-- COMMENT ON TABLE ma_table IS 'Description de la table';
-- CREATE INDEX idx_ma_table_nom ON ma_table(nom);

-- 2. AJOUTER UNE COLONNE
-- ALTER TABLE utilisateur
-- ADD COLUMN IF NOT EXISTS nouvelle_colonne VARCHAR(100);
--
-- COMMENT ON COLUMN utilisateur.nouvelle_colonne IS 'Description de la colonne';

-- 3. MODIFIER UNE COLONNE
-- ALTER TABLE utilisateur
-- ALTER COLUMN nom TYPE VARCHAR(255);

-- 4. AJOUTER UNE CLÉ ÉTRANGÈRE
-- ALTER TABLE table_source
-- ADD CONSTRAINT fk_nom_relation FOREIGN KEY (colonne_uuid)
--     REFERENCES table_cible(uuid) ON DELETE CASCADE;

-- 5. SUPPRIMER UNE CONTRAINTE
-- ALTER TABLE ma_table
-- DROP CONSTRAINT IF EXISTS fk_ancienne_relation;

-- 6. CRÉER UN INDEX
-- CREATE INDEX IF NOT EXISTS idx_ma_table_colonne ON ma_table(colonne);

-- 7. RENOMMER UNE COLONNE (avec FK)
-- -- Supprimer la FK
-- ALTER TABLE deal DROP CONSTRAINT IF EXISTS fk_deal_createur;
-- -- Renommer
-- ALTER TABLE deal RENAME COLUMN createur_uuid TO proprietaire_uuid;
-- -- Recréer la FK
-- ALTER TABLE deal ADD CONSTRAINT fk_deal_proprietaire
--     FOREIGN KEY (proprietaire_uuid) REFERENCES utilisateur(uuid) ON DELETE CASCADE;
-- -- Recréer l'index
-- DROP INDEX IF EXISTS idx_deal_createur_uuid;
-- CREATE INDEX idx_deal_proprietaire_uuid ON deal(proprietaire_uuid);

-- =========================================================================
-- RÈGLES IMPORTANTES:
-- - Toujours utiliser IF EXISTS / IF NOT EXISTS pour l'idempotence
-- - Toujours ajouter des commentaires avec COMMENT ON
-- - Toujours créer des index sur les clés étrangères
-- - Toujours définir ON DELETE (CASCADE, RESTRICT, SET NULL)
-- =========================================================================

