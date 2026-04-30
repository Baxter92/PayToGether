-- =========================================================================
-- Migration: V9__contrainte_unique_commande_utilisateur
-- Date: 2026-05-01
-- Description: Supprime les doublons dans commande_utilisateur et ajoute
--              une contrainte UNIQUE sur (commande_uuid, utilisateur_uuid)
--              pour empêcher la création de futures entrées dupliquées.
-- =========================================================================

-- Étape 1 : Supprimer les doublons en conservant la ligne la plus récente
-- (dateModification la plus grande) pour chaque paire (commande_uuid, utilisateur_uuid)
DELETE FROM commande_utilisateur cu
WHERE cu.uuid NOT IN (
    SELECT DISTINCT ON (commande_uuid, utilisateur_uuid) uuid
    FROM commande_utilisateur
    ORDER BY commande_uuid, utilisateur_uuid, date_modification DESC
);

-- Étape 2 : Ajouter la contrainte UNIQUE pour éviter les futurs doublons
ALTER TABLE commande_utilisateur
    ADD CONSTRAINT uq_commande_utilisateur UNIQUE (commande_uuid, utilisateur_uuid);

COMMENT ON CONSTRAINT uq_commande_utilisateur ON commande_utilisateur
    IS 'Empêche les doublons : un utilisateur ne peut être lié qu''une seule fois à une commande';

