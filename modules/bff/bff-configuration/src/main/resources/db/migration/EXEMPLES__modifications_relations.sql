-- =========================================================================
-- EXEMPLES DE MODIFICATIONS DE RELATIONS JPA
-- NE PAS APPLIQUER - Juste pour référence
-- =========================================================================

-- =========================================================================
-- CAS 1 : OneToOne → ManyToOne (Une adresse → Plusieurs adresses)
-- =========================================================================
-- AVANT : Un utilisateur a UNE adresse (OneToOne)
-- APRÈS : Un utilisateur a PLUSIEURS adresses (ManyToOne)
-- =========================================================================

-- Migration: V99__EXEMPLE_onetoone_to_manytoone.sql

-- ÉTAPE 1: Supprimer la contrainte UNIQUE si elle existe
ALTER TABLE adresse
DROP CONSTRAINT IF EXISTS uk_adresse_utilisateur;

-- ÉTAPE 2: La relation ManyToOne ne nécessite PAS de modification de structure
-- La colonne utilisateur_uuid reste la même, mais peut maintenant avoir des doublons

-- ÉTAPE 3: Ajouter une colonne pour identifier l'adresse principale
ALTER TABLE adresse
ADD COLUMN IF NOT EXISTS est_principale BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN adresse.est_principale IS 'Indique si c''est l''adresse principale de l''utilisateur';

-- ÉTAPE 4: Mettre à jour les adresses existantes (toutes deviennent principales)
UPDATE adresse SET est_principale = TRUE WHERE est_principale IS NULL;

-- ÉTAPE 5: Créer un index pour les requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_adresse_est_principale ON adresse(est_principale);

-- RÉSULTAT:
-- AVANT: utilisateur.adresse (OneToOne) → 1 utilisateur = 1 adresse
-- APRÈS: utilisateur.adresses (OneToMany/ManyToOne) → 1 utilisateur = N adresses

-- =========================================================================
-- CAS 2 : ManyToOne → OneToOne (Plusieurs adresses → Une adresse)
-- =========================================================================
-- AVANT : Un utilisateur a PLUSIEURS adresses (ManyToOne)
-- APRÈS : Un utilisateur a UNE adresse (OneToOne)
-- =========================================================================

-- Migration: V100__EXEMPLE_manytoone_to_onetoone.sql

-- ⚠️ ATTENTION : Nécessite une stratégie de migration des données !

-- ÉTAPE 1: Identifier les utilisateurs avec plusieurs adresses
-- SELECT utilisateur_uuid, COUNT(*) as nb_adresses
-- FROM adresse
-- GROUP BY utilisateur_uuid
-- HAVING COUNT(*) > 1;

-- ÉTAPE 2: Décider quelle adresse garder (exemple: garder l'adresse principale)
-- Option A: Supprimer les adresses secondaires
DELETE FROM adresse
WHERE uuid NOT IN (
    SELECT MIN(uuid)
    FROM adresse
    GROUP BY utilisateur_uuid
);

-- Option B: Créer une table d'archive pour les adresses supprimées
-- CREATE TABLE adresse_archive AS SELECT * FROM adresse WHERE est_principale = FALSE;
-- DELETE FROM adresse WHERE est_principale = FALSE;

-- ÉTAPE 3: Ajouter une contrainte UNIQUE sur utilisateur_uuid
ALTER TABLE adresse
ADD CONSTRAINT uk_adresse_utilisateur UNIQUE (utilisateur_uuid);

-- ÉTAPE 4: Supprimer la colonne est_principale devenue inutile
ALTER TABLE adresse
DROP COLUMN IF EXISTS est_principale;

-- RÉSULTAT:
-- AVANT: utilisateur.adresses (OneToMany/ManyToOne) → 1 utilisateur = N adresses
-- APRÈS: utilisateur.adresse (OneToOne) → 1 utilisateur = 1 adresse

-- =========================================================================
-- CAS 3 : ManyToMany → Supprimer la relation (avec table de jointure)
-- =========================================================================
-- Exemple: Supprimer la relation ManyToMany entre Deal et Tag
-- =========================================================================

-- Migration: V101__EXEMPLE_suppression_manytomany.sql

-- ÉTAPE 1: Supprimer la table de jointure
DROP TABLE IF EXISTS deal_tag CASCADE;

-- ÉTAPE 2: Supprimer la table Tag si elle n'est plus utilisée ailleurs
DROP TABLE IF EXISTS tag CASCADE;

-- RÉSULTAT:
-- La relation ManyToMany est complètement supprimée

-- =========================================================================
-- CAS 4 : Créer une relation ManyToMany (avec table de jointure)
-- =========================================================================
-- Exemple: Créer une relation ManyToMany entre Deal et Tag
-- =========================================================================

-- Migration: V102__EXEMPLE_creation_manytomany.sql

-- ÉTAPE 1: Créer la table Tag
CREATE TABLE IF NOT EXISTS tag (
    uuid UUID PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tag IS 'Tags pour catégoriser les deals (ex: Promo, Exclusif, Dernière minute)';

-- ÉTAPE 2: Créer la table de jointure
CREATE TABLE IF NOT EXISTS deal_tag (
    deal_uuid UUID NOT NULL,
    tag_uuid UUID NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (deal_uuid, tag_uuid),

    CONSTRAINT fk_deal_tag_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_deal_tag_tag FOREIGN KEY (tag_uuid)
        REFERENCES tag(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE deal_tag IS 'Table de jointure pour la relation ManyToMany entre Deal et Tag';

-- ÉTAPE 3: Créer les index
CREATE INDEX idx_deal_tag_deal_uuid ON deal_tag(deal_uuid);
CREATE INDEX idx_deal_tag_tag_uuid ON deal_tag(tag_uuid);

-- RÉSULTAT:
-- Deal → ManyToMany → Tag (un deal peut avoir plusieurs tags, un tag peut être sur plusieurs deals)

-- =========================================================================
-- CAS 5 : Ajouter une colonne dans une table de jointure ManyToMany
-- =========================================================================
-- Exemple: Ajouter un ordre de tri pour les tags d'un deal
-- =========================================================================

-- Migration: V103__EXEMPLE_ajout_colonne_table_jointure.sql

-- ÉTAPE 1: Ajouter la colonne ordre
ALTER TABLE deal_tag
ADD COLUMN IF NOT EXISTS ordre INTEGER DEFAULT 0;

COMMENT ON COLUMN deal_tag.ordre IS 'Ordre d''affichage du tag pour le deal (0 = premier)';

-- ÉTAPE 2: Mettre à jour les données existantes
UPDATE deal_tag SET ordre = 0 WHERE ordre IS NULL;

-- ÉTAPE 3: Créer un index si nécessaire
CREATE INDEX IF NOT EXISTS idx_deal_tag_ordre ON deal_tag(ordre);

-- RÉSULTAT:
-- Les tags d'un deal peuvent maintenant être triés avec la colonne "ordre"

-- =========================================================================
-- CAS 6 : Changer le type de suppression (CASCADE → RESTRICT)
-- =========================================================================
-- Exemple: Empêcher de supprimer une catégorie utilisée par des deals
-- =========================================================================

-- Migration: V104__EXEMPLE_modification_on_delete.sql

-- ÉTAPE 1: Supprimer la contrainte existante
ALTER TABLE deal
DROP CONSTRAINT IF EXISTS fk_deal_categorie;

-- ÉTAPE 2: Recréer la contrainte avec ON DELETE RESTRICT
ALTER TABLE deal
ADD CONSTRAINT fk_deal_categorie
    FOREIGN KEY (categorie_uuid)
    REFERENCES categorie(uuid)
    ON DELETE RESTRICT;

COMMENT ON CONSTRAINT fk_deal_categorie ON deal IS 'Empêche la suppression d''une catégorie utilisée par des deals';

-- RÉSULTAT:
-- AVANT: Supprimer une catégorie supprimait tous ses deals (CASCADE)
-- APRÈS: Impossible de supprimer une catégorie si elle a des deals (RESTRICT)

-- =========================================================================
-- CAS 7 : Ajouter une relation optionnelle (nullable FK)
-- =========================================================================
-- Exemple: Ajouter une relation vers un parrain (utilisateur)
-- =========================================================================

-- Migration: V105__EXEMPLE_ajout_relation_optionnelle.sql

-- ÉTAPE 1: Ajouter la colonne pour la clé étrangère (nullable)
ALTER TABLE utilisateur
ADD COLUMN IF NOT EXISTS parrain_uuid UUID;

COMMENT ON COLUMN utilisateur.parrain_uuid IS 'UUID de l''utilisateur qui a parrainé (optionnel)';

-- ÉTAPE 2: Ajouter la contrainte de clé étrangère
ALTER TABLE utilisateur
ADD CONSTRAINT fk_utilisateur_parrain
    FOREIGN KEY (parrain_uuid)
    REFERENCES utilisateur(uuid)
    ON DELETE SET NULL;

-- ÉTAPE 3: Créer un index
CREATE INDEX IF NOT EXISTS idx_utilisateur_parrain ON utilisateur(parrain_uuid);

-- RÉSULTAT:
-- Un utilisateur peut maintenant avoir un parrain (relation OneToMany auto-référencée)

-- =========================================================================
-- CAS 8 : Supprimer une relation (FK) et la colonne
-- =========================================================================
-- Exemple: Supprimer la relation vers un parrain
-- =========================================================================

-- Migration: V106__EXEMPLE_suppression_relation.sql

-- ÉTAPE 1: Supprimer la contrainte de clé étrangère
ALTER TABLE utilisateur
DROP CONSTRAINT IF EXISTS fk_utilisateur_parrain;

-- ÉTAPE 2: Supprimer l'index
DROP INDEX IF EXISTS idx_utilisateur_parrain;

-- ÉTAPE 3: Supprimer la colonne
ALTER TABLE utilisateur
DROP COLUMN IF EXISTS parrain_uuid;

-- RÉSULTAT:
-- La relation vers le parrain est complètement supprimée

-- =========================================================================
-- CAS 9 : Transformer une FK simple en FK composite (clé composée)
-- =========================================================================
-- Exemple: Relation vers une table avec clé primaire composée
-- =========================================================================

-- Migration: V107__EXEMPLE_fk_composite.sql

-- ⚠️ CAS RARE : Utiliser uniquement si la table cible a une PK composite

-- ÉTAPE 1: Créer une table avec clé primaire composée
CREATE TABLE IF NOT EXISTS configuration (
    module VARCHAR(50) NOT NULL,
    cle VARCHAR(50) NOT NULL,
    valeur TEXT,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (module, cle)
);

COMMENT ON TABLE configuration IS 'Configuration du système (clé composée: module + cle)';

-- ÉTAPE 2: Créer une table qui référence cette clé composée
CREATE TABLE IF NOT EXISTS historique_configuration (
    uuid UUID PRIMARY KEY,
    module VARCHAR(50) NOT NULL,
    cle VARCHAR(50) NOT NULL,
    ancienne_valeur TEXT,
    nouvelle_valeur TEXT,
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_historique_configuration
        FOREIGN KEY (module, cle)
        REFERENCES configuration(module, cle)
        ON DELETE CASCADE
);

COMMENT ON TABLE historique_configuration IS 'Historique des modifications de configuration';

-- ÉTAPE 3: Créer les index
CREATE INDEX idx_historique_config_module_cle ON historique_configuration(module, cle);

-- RÉSULTAT:
-- Relation vers une table avec clé primaire composée (module + cle)

-- =========================================================================
-- CAS 10 : Ajouter une relation bidirectionnelle (owner + inverse)
-- =========================================================================
-- Exemple: Deal ↔ Commentaire (bidirectionnel)
-- =========================================================================

-- Migration: V108__EXEMPLE_relation_bidirectionnelle.sql

-- La structure SQL reste identique, seule la configuration JPA change
-- Il suffit d'avoir la FK dans la table enfant (commentaire)

-- Vérifier que la relation existe
-- SELECT
--     tc.constraint_name,
--     tc.table_name,
--     kcu.column_name,
--     ccu.table_name AS foreign_table_name
-- FROM information_schema.table_constraints AS tc
-- JOIN information_schema.key_column_usage AS kcu
--     ON tc.constraint_name = kcu.constraint_name
-- JOIN information_schema.constraint_column_usage AS ccu
--     ON ccu.constraint_name = tc.constraint_name
-- WHERE tc.constraint_type = 'FOREIGN KEY'
--     AND tc.table_name = 'commentaire'
--     AND kcu.column_name = 'deal_uuid';

-- Si la FK n'existe pas, la créer:
ALTER TABLE commentaire
ADD CONSTRAINT fk_commentaire_deal
    FOREIGN KEY (deal_uuid)
    REFERENCES deal(uuid)
    ON DELETE CASCADE;

-- RÉSULTAT (côté JPA):
-- Deal: @OneToMany(mappedBy = "deal") List<Commentaire> commentaires;
-- Commentaire: @ManyToOne @JoinColumn(name = "deal_uuid") Deal deal;

-- =========================================================================
-- RÉSUMÉ DES PATTERNS
-- =========================================================================

-- 1. OneToOne → ManyToOne : Supprimer UNIQUE, ajouter colonne "est_principal"
-- 2. ManyToOne → OneToOne : Nettoyer les doublons, ajouter UNIQUE
-- 3. Supprimer ManyToMany : DROP table de jointure
-- 4. Créer ManyToMany : Créer table + table de jointure
-- 5. Modifier table jointure : ALTER TABLE add column
-- 6. Changer ON DELETE : DROP + ADD constraint
-- 7. Ajouter FK optionnelle : ADD COLUMN nullable + FK
-- 8. Supprimer FK : DROP constraint + DROP column
-- 9. FK composite : PRIMARY KEY (col1, col2), FOREIGN KEY (col1, col2)
-- 10. Bidirectionnel : Juste la FK côté enfant (JPA gère le reste)

-- =========================================================================
-- CHECKLIST POUR MODIFIER UNE RELATION
-- =========================================================================

-- ✅ 1. Identifier le type de modification (OneToOne → ManyToOne, etc.)
-- ✅ 2. Sauvegarder les données si nécessaire (CREATE TABLE backup AS SELECT...)
-- ✅ 3. Supprimer les contraintes existantes (DROP CONSTRAINT)
-- ✅ 4. Modifier la structure (ALTER TABLE, ADD/DROP COLUMN)
-- ✅ 5. Nettoyer les données si nécessaire (DELETE, UPDATE)
-- ✅ 6. Recréer les contraintes (ADD CONSTRAINT)
-- ✅ 7. Mettre à jour les index (DROP + CREATE INDEX)
-- ✅ 8. Ajouter les commentaires (COMMENT ON)
-- ✅ 9. Tester avec quelques requêtes SELECT
-- ✅ 10. Mettre à jour les entités JPA correspondantes

-- =========================================================================
-- FIN DES EXEMPLES
-- =========================================================================

