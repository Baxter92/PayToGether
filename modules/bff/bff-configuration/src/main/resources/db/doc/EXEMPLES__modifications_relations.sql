-- =========================================================================
-- EXEMPLES COMPLETS : Modifications de relations JPA
-- Date: 2026-03-06
-- Auteur: Équipe PayToGether
-- Description: 10 cas d'usage pour modifier les relations entre entités
-- =========================================================================
--
-- ⚠️ IMPORTANT : Ces exemples sont basés sur les entités JPA réelles du projet
-- ⚠️ NE PAS APPLIQUER CES MIGRATIONS - Ce sont des EXEMPLES de référence
--
-- CAS COUVERTS :
-- 1. OneToOne → ManyToOne (Commande : 1 marchand → plusieurs commandes)
-- 2. ManyToOne → OneToOne (Adresse : plusieurs adresses → une adresse)
-- 3. Supprimer une relation ManyToMany (Deal participants)
-- 4. Créer une relation ManyToMany (Utilisateur favoris)
-- 5. Modifier une table de jointure (Ajouter date_ajout)
-- 6. Changer ON DELETE (CASCADE → RESTRICT)
-- 7. Ajouter une FK optionnelle (parrain)
-- 8. Supprimer une FK (et la colonne)
-- 9. Renommer une FK (utilisateur_uuid → marchand_uuid dans Deal)
-- 10. Relation auto-référencée (Commentaire parent)
-- =========================================================================

-- =========================================================================
-- CAS 1 : OneToOne → ManyToOne
-- =========================================================================
-- Contexte : CommandeJpa a @OneToOne avec UtilisateurJpa (marchandJpa)
--            Un marchand ne peut avoir qu'une seule commande (AVANT)
-- Besoin : Permettre qu'un marchand ait plusieurs commandes (APRÈS)
-- Solution : Transformer OneToOne en ManyToOne
-- =========================================================================

-- V20__commande_onetoone_vers_manytoone.sql

-- 1. Supprimer la contrainte d'unicité (imposée par OneToOne)
--    Cette contrainte empêche plusieurs commandes d'avoir le même utilisateur_uuid
ALTER TABLE commande DROP CONSTRAINT IF EXISTS uk_commande_utilisateur;

-- 2. La FK existe déjà (utilisateur_uuid), on la garde
--    Aucune modification nécessaire sur la colonne elle-même

-- 3. Ajouter un index si pas déjà présent (pour performance des requêtes)
CREATE INDEX IF NOT EXISTS idx_commande_utilisateur_uuid ON commande(utilisateur_uuid);

-- 4. Mettre à jour le commentaire pour refléter la nouvelle relation
COMMENT ON COLUMN commande.utilisateur_uuid IS 'Marchand ayant créé la commande (ManyToOne - un marchand peut avoir plusieurs commandes)';

-- ✅ Résultat : Plusieurs commandes peuvent avoir le même utilisateur_uuid
-- ⚠️ Dans CommandeJpa.java, changer @OneToOne en @ManyToOne

-- =========================================================================
-- CAS 2 : ManyToOne → OneToOne
-- =========================================================================
-- Contexte : AdresseJpa a @OneToOne avec UtilisateurJpa
--            Mais rien n'empêche en base qu'un utilisateur ait plusieurs adresses (AVANT)
-- Besoin : S'assurer qu'un utilisateur n'a qu'une seule adresse (APRÈS)
-- Solution : Ajouter une contrainte d'unicité
-- =========================================================================

-- V21__adresse_manytoone_vers_onetoone.sql

-- 1. Vérifier qu'il n'y a pas de doublons (IMPORTANT : nettoyer avant la migration)
DO $$
BEGIN
    IF EXISTS (
        SELECT utilisateur_uuid
        FROM adresse
        GROUP BY utilisateur_uuid
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'ERREUR : Des utilisateurs ont plusieurs adresses. Nettoyer les données avant d''appliquer cette migration.';
    END IF;
END $$;

-- 2. Ajouter contrainte d'unicité (impose OneToOne)
--    Cela empêchera plusieurs adresses d'avoir le même utilisateur_uuid
ALTER TABLE adresse
ADD CONSTRAINT uk_adresse_utilisateur
UNIQUE (utilisateur_uuid);

-- 3. Mettre à jour le commentaire
COMMENT ON COLUMN adresse.utilisateur_uuid IS 'Utilisateur propriétaire de l''adresse (OneToOne - un utilisateur ne peut avoir qu''une seule adresse)';

-- ✅ Résultat : Un utilisateur ne peut avoir qu'une seule adresse
-- ⚠️ Garder @OneToOne dans AdresseJpa.java

-- =========================================================================
-- CAS 3 : Supprimer une relation ManyToMany
-- =========================================================================
-- Contexte : DealJpa a @ManyToMany avec UtilisateurJpa (participants)
--            Table de jointure : deal_participants
-- Besoin : Supprimer cette relation car on utilise une table Commande à la place
-- =========================================================================

-- V22__supprimer_relation_deal_participants.sql

-- 1. Supprimer la table de jointure
--    CASCADE supprime automatiquement les FK liées
DROP TABLE IF EXISTS deal_participants CASCADE;

-- 2. Nettoyer les index orphelins (si existants)
DROP INDEX IF EXISTS idx_deal_participants_deal;
DROP INDEX IF EXISTS idx_deal_participants_utilisateur;

-- ✅ Résultat : La relation ManyToMany n'existe plus
-- ⚠️ Supprimer l'annotation @ManyToMany dans DealJpa.java :
--    @ManyToMany
--    @JoinTable(name = "deal_participants", ...)
--    private Set<UtilisateurJpa> participants = new HashSet<>();

-- =========================================================================
-- CAS 4 : Créer une relation ManyToMany
-- =========================================================================
-- Besoin : Ajouter une relation "favoris" entre Utilisateur et Deal
--          Un utilisateur peut avoir plusieurs deals favoris
--          Un deal peut être favori pour plusieurs utilisateurs
-- Solution : Créer une table de jointure
-- =========================================================================

-- V23__ajout_relation_favoris.sql

-- 1. Créer la table de jointure
--    Clé primaire composée (utilisateur_uuid, deal_uuid) empêche les doublons
CREATE TABLE utilisateur_favoris (
    utilisateur_uuid UUID NOT NULL,
    deal_uuid UUID NOT NULL,
    date_ajout TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Clé primaire composée
    PRIMARY KEY (utilisateur_uuid, deal_uuid),

    -- FK vers utilisateur
    CONSTRAINT fk_favoris_utilisateur
        FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid)
        ON DELETE CASCADE,

    -- FK vers deal
    CONSTRAINT fk_favoris_deal
        FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid)
        ON DELETE CASCADE
);

-- 2. Créer les index pour améliorer la performance des requêtes
CREATE INDEX idx_utilisateur_favoris_utilisateur ON utilisateur_favoris(utilisateur_uuid);
CREATE INDEX idx_utilisateur_favoris_deal ON utilisateur_favoris(deal_uuid);

-- 3. Ajouter un commentaire explicatif
COMMENT ON TABLE utilisateur_favoris IS 'Table de jointure pour les deals favoris des utilisateurs (ManyToMany)';
COMMENT ON COLUMN utilisateur_favoris.date_ajout IS 'Date à laquelle l''utilisateur a ajouté ce deal à ses favoris';

-- ✅ Résultat : Relation ManyToMany fonctionnelle

-- ⚠️ Ajouter dans UtilisateurJpa.java :
-- @ManyToMany
-- @JoinTable(name = "utilisateur_favoris",
--     joinColumns = @JoinColumn(name = "utilisateur_uuid"),
--     inverseJoinColumns = @JoinColumn(name = "deal_uuid"))
-- private Set<DealJpa> dealsFavoris = new HashSet<>();

-- =========================================================================
-- CAS 5 : Modifier une table de jointure ManyToMany (ajouter colonnes)
-- =========================================================================
-- Contexte : Table deal_participants existante (relation ManyToMany)
-- Besoin : Ajouter la date d'inscription et un statut pour suivre les participants
-- Solution : Enrichir la table de jointure
-- =========================================================================

-- V24__enrichir_table_jointure_participants.sql

-- 1. Ajouter des colonnes métier
ALTER TABLE deal_participants
ADD COLUMN IF NOT EXISTS date_inscription TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE deal_participants
ADD COLUMN IF NOT EXISTS statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF';

ALTER TABLE deal_participants
ADD COLUMN IF NOT EXISTS date_modification TIMESTAMP NOT NULL DEFAULT NOW();

-- 2. Créer un index sur le statut (pour filtrer les participants actifs)
CREATE INDEX idx_deal_participants_statut ON deal_participants(statut);

-- 3. Ajouter des commentaires
COMMENT ON COLUMN deal_participants.date_inscription IS 'Date à laquelle l''utilisateur a rejoint le deal';
COMMENT ON COLUMN deal_participants.statut IS 'Statut du participant : ACTIF, INACTIF, BANNI';

-- ✅ Résultat : La table de jointure devient une entité à part entière avec métadonnées

-- ⚠️ Si besoin de gérer ces champs en JPA, créer une entité :
-- DealParticipantJpa avec une clé primaire composée

-- =========================================================================
-- CAS 6 : Changer la stratégie ON DELETE
-- =========================================================================
-- Contexte : Deal a une FK vers Categorie avec ON DELETE CASCADE (actuellement)
--            Supprimer une catégorie supprime tous les deals associés
-- Besoin : Empêcher la suppression d'une catégorie si des deals l'utilisent
-- Solution : Changer CASCADE → RESTRICT
-- =========================================================================

-- V25__modifier_on_delete_categorie.sql

-- 1. Supprimer la FK existante
ALTER TABLE deal DROP CONSTRAINT IF EXISTS fk_deal_categorie;

-- 2. Recréer la FK avec ON DELETE RESTRICT
--    RESTRICT empêche la suppression si des deals référencent la catégorie
ALTER TABLE deal
ADD CONSTRAINT fk_deal_categorie
    FOREIGN KEY (categorie_uuid)
    REFERENCES categorie(uuid)
    ON DELETE RESTRICT;

-- ✅ Résultat : Impossible de supprimer une catégorie si des deals l'utilisent
-- Si on essaie, PostgreSQL retournera une erreur

-- =========================================================================
-- CAS 7 : Ajouter une FK optionnelle (nullable)
-- =========================================================================
-- Besoin : Ajouter une relation optionnelle "parrain" sur Utilisateur
--          Un utilisateur peut être parrainé par un autre utilisateur
--          Relation auto-référencée (utilisateur → utilisateur)
-- =========================================================================

-- V26__ajout_relation_parrain.sql

-- 1. Ajouter la colonne (nullable par défaut = optionnelle)
ALTER TABLE utilisateur
ADD COLUMN parrain_uuid UUID;

-- 2. Créer la FK (auto-référencée vers la même table)
ALTER TABLE utilisateur
ADD CONSTRAINT fk_utilisateur_parrain
    FOREIGN KEY (parrain_uuid)
    REFERENCES utilisateur(uuid)
    ON DELETE SET NULL;  -- Si le parrain est supprimé, on met NULL

-- 3. Créer un index pour les requêtes "Tous les filleuls d'un parrain"
CREATE INDEX idx_utilisateur_parrain ON utilisateur(parrain_uuid);

-- 4. Ajouter un commentaire
COMMENT ON COLUMN utilisateur.parrain_uuid IS 'Utilisateur parrain (optionnel, auto-référencé)';

-- ✅ Résultat : Un utilisateur peut avoir un parrain ou non (NULL autorisé)

-- ⚠️ Ajouter dans UtilisateurJpa.java :
-- @ManyToOne
-- @JoinColumn(name = "parrain_uuid")
-- private UtilisateurJpa parrain;

-- =========================================================================
-- CAS 8 : Supprimer une FK (et la colonne)
-- =========================================================================
-- Contexte : Commentaire avait une colonne "note" avec un INTEGER
-- Besoin : On n'utilise plus cette colonne, il faut la supprimer
-- =========================================================================

-- V27__supprimer_colonne_note_commentaire.sql

-- 1. Supprimer les contraintes liées à la colonne (CHECK, par exemple)
ALTER TABLE commentaire DROP CONSTRAINT IF EXISTS chk_commentaire_note;

-- 2. Supprimer les index liés à la colonne
DROP INDEX IF EXISTS idx_commentaire_note;

-- 3. Supprimer la colonne
--    Les données de cette colonne seront DÉFINITIVEMENT PERDUES
ALTER TABLE commentaire DROP COLUMN IF EXISTS note;

-- ✅ Résultat : La colonne note n'existe plus

-- ⚠️ Supprimer l'attribut dans CommentaireJpa.java :
-- @Column(nullable = false)
-- private Integer note;

-- =========================================================================
-- CAS 9 : Renommer une FK (et la colonne)
-- =========================================================================
-- Contexte : Deal a une colonne "utilisateur_uuid" qu'on veut renommer en "marchand_uuid"
--            Meilleure sémantique pour le modèle métier
-- Besoin : Renommer tout en gardant la FK fonctionnelle
-- =========================================================================

-- V28__renommer_utilisateur_en_marchand.sql

-- 1. Supprimer la FK existante (impossible de renommer une colonne avec FK active)
ALTER TABLE deal DROP CONSTRAINT IF EXISTS fk_deal_utilisateur;

-- 2. Renommer la colonne
ALTER TABLE deal RENAME COLUMN utilisateur_uuid TO marchand_uuid;

-- 3. Recréer la FK avec le nouveau nom de colonne
ALTER TABLE deal
ADD CONSTRAINT fk_deal_marchand
    FOREIGN KEY (marchand_uuid)
    REFERENCES utilisateur(uuid)
    ON DELETE CASCADE;

-- 4. Renommer l'index
DROP INDEX IF EXISTS idx_deal_utilisateur_uuid;
CREATE INDEX idx_deal_marchand_uuid ON deal(marchand_uuid);

-- 5. Mettre à jour le commentaire
COMMENT ON COLUMN deal.marchand_uuid IS 'Marchand ayant créé le deal';

-- ✅ Résultat : utilisateur_uuid devient marchand_uuid dans la table deal

-- ⚠️ Renommer l'attribut dans DealJpa.java :
-- @ManyToOne
-- @JoinColumn(name = "marchand_uuid", nullable = false)
-- private UtilisateurJpa marchandJpa;

-- =========================================================================
-- CAS 10 : Relation auto-référencée (Commentaire parent)
-- =========================================================================
-- Contexte : CommentaireJpa a @ManyToOne commentaireParentJpa
--            Un commentaire peut répondre à un autre commentaire
-- Besoin : S'assurer que la FK existe et fonctionne correctement
-- =========================================================================

-- V29__verifier_relation_commentaire_parent.sql

-- 1. Vérifier que la FK existe, sinon la créer
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_commentaire_parent'
        AND table_name = 'commentaire'
    ) THEN
        -- 2. Créer la FK si elle n'existe pas
        ALTER TABLE commentaire
        ADD CONSTRAINT fk_commentaire_parent
            FOREIGN KEY (commentaire_parent_uuid)
            REFERENCES commentaire(uuid)
            ON DELETE CASCADE;  -- Si le parent est supprimé, les réponses aussi
    END IF;
END $$;

-- 3. Créer l'index si pas présent
CREATE INDEX IF NOT EXISTS idx_commentaire_parent ON commentaire(commentaire_parent_uuid);

-- 4. Mettre à jour le commentaire
COMMENT ON COLUMN commentaire.commentaire_parent_uuid IS 'Commentaire parent pour les réponses (auto-référencé, nullable)';

-- ✅ Résultat : Un commentaire peut répondre à un autre commentaire (hiérarchie)
-- Si commentaire_parent_uuid IS NULL → Commentaire racine
-- Si commentaire_parent_uuid IS NOT NULL → Réponse à un autre commentaire

-- =========================================================================
-- BONUS : Template générique pour modifier une FK
-- =========================================================================
-- À copier et adapter selon vos besoins

/*
-- V30__template_modification_fk.sql

-- 1. Supprimer la FK existante
ALTER TABLE {table_enfant} DROP CONSTRAINT IF EXISTS fk_{nom_relation};

-- 2. Modifier la colonne si besoin
-- Rendre obligatoire :
ALTER TABLE {table_enfant} ALTER COLUMN {colonne_fk} SET NOT NULL;
-- Rendre optionnelle :
ALTER TABLE {table_enfant} ALTER COLUMN {colonne_fk} DROP NOT NULL;
-- Renommer :
ALTER TABLE {table_enfant} RENAME COLUMN {ancienne_colonne} TO {nouvelle_colonne};

-- 3. Recréer la FK avec la nouvelle configuration
ALTER TABLE {table_enfant}
ADD CONSTRAINT fk_{nouveau_nom}
    FOREIGN KEY ({colonne_fk})
    REFERENCES {table_parent}(uuid)
    ON DELETE {CASCADE | RESTRICT | SET NULL | NO ACTION};

-- 4. Gérer les index
DROP INDEX IF EXISTS idx_{ancien_nom};
CREATE INDEX idx_{nouveau_nom} ON {table_enfant}({colonne_fk});

-- 5. Ajouter un commentaire
COMMENT ON COLUMN {table_enfant}.{colonne_fk} IS 'Description claire de la relation';
*/

-- =========================================================================
-- Stratégies ON DELETE expliquées
-- =========================================================================

-- CASCADE : Si le parent est supprimé, les enfants sont supprimés aussi
--           Exemple : Supprimer un deal supprime toutes ses images
--           ALTER TABLE image_deal ... ON DELETE CASCADE

-- RESTRICT : Empêche la suppression du parent si des enfants existent
--            Exemple : Empêcher de supprimer une catégorie si des deals l'utilisent
--            ALTER TABLE deal ... ON DELETE RESTRICT

-- SET NULL : Si le parent est supprimé, la FK de l'enfant devient NULL
--            Exemple : Si un parrain est supprimé, parrain_uuid devient NULL
--            ALTER TABLE utilisateur ... ON DELETE SET NULL
--            ⚠️ Nécessite que la colonne soit nullable

-- NO ACTION : Similaire à RESTRICT (comportement par défaut)

-- =========================================================================
-- Récapitulatif des cas d'usage
-- =========================================================================

-- ✅ OneToOne → ManyToOne : Supprimer contrainte d'unicité
-- ✅ ManyToOne → OneToOne : Ajouter contrainte d'unicité
-- ✅ Supprimer ManyToMany : DROP TABLE de jointure
-- ✅ Créer ManyToMany : CREATE TABLE de jointure
-- ✅ Enrichir table jointure : ALTER TABLE ADD COLUMN
-- ✅ Changer ON DELETE : DROP + ADD CONSTRAINT
-- ✅ FK optionnelle : ADD COLUMN nullable + FK
-- ✅ Supprimer FK : DROP CONSTRAINT + DROP COLUMN
-- ✅ Renommer FK : DROP + RENAME + ADD CONSTRAINT
-- ✅ Auto-référencée : FK vers la même table

-- =========================================================================
-- FIN DES EXEMPLES
-- =========================================================================

-- ⚠️ RAPPEL IMPORTANT :
-- 1. Toujours vérifier vos entités JPA avant de créer une migration
-- 2. Tester localement avant de pousser
-- 3. Ne jamais modifier une migration déjà appliquée
-- 4. Toujours ajouter des commentaires SQL
-- 5. Toujours créer des index sur les FK
-- 6. Toujours gérer ON DELETE selon la logique métier

-- Date de dernière mise à jour : 6 mars 2026
-- Auteur : Équipe PayToGether

