# 📚 Guide Complet : Migrations SQL avec Flyway

## 🎯 Introduction

Ce projet utilise **Flyway** pour gérer les migrations de base de données avec des **scripts SQL purs**. Simple, robuste et sans XML !

---

## 📁 Structure

```
db/migration/
├── V1__schema_initial.sql              ← Migration initiale
├── V2__ajout_table_paiement.sql        ← Ajout d'une table
├── V3__ajout_telephone_utilisateur.sql ← Modification de table
├── TEMPLATE__migration.sql             ← Template à copier
├── EXEMPLE__modification_relation.sql  ← Exemple modification FK
└── GUIDE_RAPIDE.md                     ← Guide rapide (ce fichier)
```

---

## 🚀 Comment Flyway fonctionne

1. Au démarrage de l'application, Flyway scanne le dossier `db/migration/`
2. Il compare les fichiers SQL avec la table `flyway_schema_history`
3. Il applique automatiquement les nouvelles migrations **dans l'ordre** (V1, V2, V3...)
4. Chaque migration réussie est enregistrée avec un **checksum** (hash MD5)

**Important** : Une fois appliquée, une migration ne peut plus être modifiée (Flyway détecte le changement de checksum).

---

## 📝 Conventions de nommage

### Format obligatoire

`V{VERSION}__{DESCRIPTION}.sql`

- **V** : Préfixe obligatoire (majuscule)
- **VERSION** : Nombre entier croissant (1, 2, 3, 10, 100...)
- **__** : Double underscore (séparateur obligatoire)
- **DESCRIPTION** : En snake_case (mots séparés par `_`)

### Exemples valides

```
V1__schema_initial.sql
V2__ajout_table_paiement.sql
V3__ajout_telephone_utilisateur.sql
V10__refonte_utilisateur.sql
V100__migration_majeure.sql
```

### Exemples invalides

```
V1.0.0__schema.sql          ❌ Pas de points dans la version
V2-ajout-table.sql          ❌ Utiliser __ et _
migration_1.sql             ❌ Doit commencer par V
v3__lowercase.sql           ❌ V doit être majuscule
V4_single_underscore.sql    ❌ Utiliser double underscore __
```

---

## ✅ Créer une nouvelle migration

### Étape 1 : Copier le template

```bash
cp db/migration/TEMPLATE__migration.sql \
   db/migration/V4__ajout_table_adresse.sql
```

### Étape 2 : Éditer le fichier SQL

```sql
-- =========================================================================
-- Migration: V4__ajout_table_adresse
-- Date: 2026-03-05
-- Auteur: Ton nom
-- Description: Ajout de la table adresse pour les utilisateurs
-- =========================================================================

CREATE TABLE IF NOT EXISTS adresse (
    uuid UUID PRIMARY KEY,
    utilisateur_uuid UUID NOT NULL,
    rue VARCHAR(255) NOT NULL,
    ville VARCHAR(100) NOT NULL,
    code_postal VARCHAR(20) NOT NULL,
    pays VARCHAR(100) NOT NULL DEFAULT 'Canada',
    est_principale BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_adresse_utilisateur 
        FOREIGN KEY (utilisateur_uuid) 
        REFERENCES utilisateur(uuid) 
        ON DELETE CASCADE
);

COMMENT ON TABLE adresse IS 'Adresses postales des utilisateurs';
COMMENT ON COLUMN adresse.est_principale IS 'Indique l''adresse principale de l''utilisateur';

CREATE INDEX idx_adresse_utilisateur_uuid ON adresse(utilisateur_uuid);
CREATE INDEX idx_adresse_est_principale ON adresse(est_principale);
```

### Étape 3 : Redémarrer l'application

```bash
mvn spring-boot:run
```

**Dans les logs**, tu verras :

```
INFO 12345 --- [main] o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "4 - ajout table adresse"
INFO 12345 --- [main] o.f.core.internal.command.DbMigrate : Successfully applied 1 migration to schema "public"
```

---

## 🔄 Cas d'usage courants

### 1. Créer une table

```sql
-- V5__creation_table_commande.sql

CREATE TABLE IF NOT EXISTS commande (
    uuid UUID PRIMARY KEY,
    utilisateur_uuid UUID NOT NULL,
    deal_uuid UUID NOT NULL,
    montant DECIMAL(10, 2) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_commande_utilisateur FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_commande_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE RESTRICT
);

COMMENT ON TABLE commande IS 'Commandes passées par les utilisateurs';
CREATE INDEX idx_commande_utilisateur ON commande(utilisateur_uuid);
CREATE INDEX idx_commande_deal ON commande(deal_uuid);
CREATE INDEX idx_commande_statut ON commande(statut);
```

### 2. Ajouter une colonne

```sql
-- V6__ajout_colonne_avatar_url.sql

ALTER TABLE utilisateur 
ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

COMMENT ON COLUMN utilisateur.avatar_url IS 'URL de l''avatar de l''utilisateur';

CREATE INDEX IF NOT EXISTS idx_utilisateur_avatar ON utilisateur(avatar_url);
```

### 3. Modifier une colonne

```sql
-- V7__agrandir_colonne_description.sql

ALTER TABLE deal 
ALTER COLUMN description TYPE TEXT;

COMMENT ON COLUMN deal.description IS 'Description complète du deal (texte illimité)';
```

### 4. Ajouter une clé étrangère

```sql
-- V8__ajout_relation_commande_paiement.sql

ALTER TABLE commande
ADD COLUMN paiement_uuid UUID;

ALTER TABLE commande
ADD CONSTRAINT fk_commande_paiement 
    FOREIGN KEY (paiement_uuid) 
    REFERENCES paiement(uuid) 
    ON DELETE SET NULL;

COMMENT ON COLUMN commande.paiement_uuid IS 'Référence au paiement associé';
CREATE INDEX idx_commande_paiement ON commande(paiement_uuid);
```

### 5. Supprimer une contrainte

```sql
-- V9__suppression_contrainte_obsolete.sql

ALTER TABLE deal 
DROP CONSTRAINT IF EXISTS fk_deal_ancienne_relation;

DROP INDEX IF EXISTS idx_deal_ancien_champ;
```

### 6. Renommer une colonne (avec FK)

```sql
-- V10__renommage_createur_proprietaire.sql

-- 1. Supprimer la FK existante
ALTER TABLE deal DROP CONSTRAINT IF EXISTS fk_deal_createur;

-- 2. Renommer la colonne
ALTER TABLE deal RENAME COLUMN createur_uuid TO proprietaire_uuid;

-- 3. Recréer la FK avec le nouveau nom
ALTER TABLE deal 
ADD CONSTRAINT fk_deal_proprietaire 
    FOREIGN KEY (proprietaire_uuid) 
    REFERENCES utilisateur(uuid) 
    ON DELETE CASCADE;

-- 4. Mettre à jour les index
DROP INDEX IF EXISTS idx_deal_createur_uuid;
CREATE INDEX idx_deal_proprietaire_uuid ON deal(proprietaire_uuid);

-- 5. Commentaires
COMMENT ON COLUMN deal.proprietaire_uuid IS 'UUID du propriétaire du deal';
```

---

## 🛡️ Bonnes pratiques SQL

### 1. Idempotence (IF EXISTS / IF NOT EXISTS)

✅ **Toujours** utiliser ces clauses pour que les migrations soient rejouables :

```sql
CREATE TABLE IF NOT EXISTS ma_table (...);
ALTER TABLE ma_table ADD COLUMN IF NOT EXISTS ma_colonne VARCHAR(100);
DROP TABLE IF EXISTS ma_table CASCADE;
DROP CONSTRAINT IF EXISTS fk_nom;
DROP INDEX IF EXISTS idx_nom;
```

### 2. Contraintes ON DELETE

✅ **Toujours** définir le comportement lors de la suppression :

```sql
-- CASCADE : Supprime les lignes enfants
CONSTRAINT fk_image_deal FOREIGN KEY (deal_uuid)
    REFERENCES deal(uuid) ON DELETE CASCADE

-- RESTRICT : Empêche la suppression si des enfants existent
CONSTRAINT fk_deal_categorie FOREIGN KEY (categorie_uuid)
    REFERENCES categorie(uuid) ON DELETE RESTRICT

-- SET NULL : Met à NULL les références
CONSTRAINT fk_commande_paiement FOREIGN KEY (paiement_uuid)
    REFERENCES paiement(uuid) ON DELETE SET NULL
```

### 3. Commentaires

✅ **Toujours** documenter les tables et colonnes :

```sql
COMMENT ON TABLE deal IS 'Deals créés par les utilisateurs';
COMMENT ON COLUMN deal.statut IS 'Statut: BROUILLON, PUBLIE, EXPIRE, ANNULE';
```

### 4. Index

✅ **Toujours** créer des index sur :
- Les clés étrangères
- Les colonnes fréquemment filtrées (WHERE, ORDER BY)
- Les colonnes de recherche

```sql
CREATE INDEX idx_deal_createur_uuid ON deal(createur_uuid);
CREATE INDEX idx_deal_statut ON deal(statut);
CREATE INDEX idx_deal_date_fin ON deal(date_fin);
```

### 5. Valeurs par défaut

✅ **Toujours** définir des valeurs par défaut :

```sql
statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
est_actif BOOLEAN DEFAULT TRUE,
pays VARCHAR(100) DEFAULT 'Canada'
```

---

## 🚨 Erreurs courantes

### Erreur 1 : Migration déjà appliquée et modifiée

```
ERROR: Validate failed: Migration checksum mismatch for migration version 2
```

**Cause** : Tu as modifié un fichier SQL déjà appliqué.

**Solution** : Créer une nouvelle migration corrective (V4, V5...) au lieu de modifier V2.

### Erreur 2 : Numérotation non séquentielle

```
ERROR: Detected resolved migration not applied to database: 5
```

**Cause** : Tu as sauté un numéro (V3 existe mais pas V4).

**Solution** : Assure-toi que les numéros se suivent (V1, V2, V3, V4...).

### Erreur 3 : Format de nom invalide

```
ERROR: Unable to parse version from filename: migration_ajout_table.sql
```

**Cause** : Le nom ne suit pas le format `V{N}__{description}.sql`.

**Solution** : Renommer en `V4__ajout_table.sql`.

---

## 🔍 Vérifier l'état des migrations

### Table Flyway

Flyway crée automatiquement la table `flyway_schema_history` :

```sql
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

**Résultat** :

| installed_rank | version | description | script | installed_on | success |
|----------------|---------|-------------|--------|--------------|---------|
| 1 | 1 | schema initial | V1__schema_initial.sql | 2026-03-05 10:00:00 | true |
| 2 | 2 | ajout table paiement | V2__ajout_table_paiement.sql | 2026-03-05 10:05:00 | true |
| 3 | 3 | ajout telephone utilisateur | V3__ajout_telephone_utilisateur.sql | 2026-03-05 10:10:00 | true |

---

## ⚙️ Configuration Flyway

### application.properties

```properties
# Activer Flyway
spring.flyway.enabled=true

# Localisation des scripts SQL
spring.flyway.locations=classpath:db/migration

# Créer une baseline si BD existe déjà
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

# Valider les migrations au démarrage
spring.flyway.validate-on-migrate=true
```

### Désactiver temporairement

```properties
# Pour désactiver (déconseillé en prod)
spring.flyway.enabled=false
```

---

## 🔄 Rollback (annulation)

⚠️ **Flyway Community Edition ne supporte pas le rollback automatique.**

Pour annuler une migration, tu dois :

1. Créer une **nouvelle migration** qui fait l'inverse
2. Appliquer cette migration correctrice

**Exemple** : Tu as créé une table en V4, pour l'annuler :

```sql
-- V5__rollback_creation_table_commande.sql

DROP TABLE IF EXISTS commande CASCADE;
```

---

## 📚 Ressources

### Templates disponibles

- 📄 **TEMPLATE__migration.sql** : Template complet à copier
- 📄 **EXEMPLE__modification_relation.sql** : Exemple de renommage FK
- 📄 **EXEMPLES__modifications_relations.sql** : 10 cas de modifications de relations (OneToOne, ManyToOne, ManyToMany, etc.)

### Guides spécialisés

- 📘 **GUIDE_RAPIDE.md** : Guide rapide en 2 étapes (5 min)
- 📗 **GUIDE_MODIFICATIONS_RELATIONS.md** : Guide visuel des modifications de relations JPA
- 📙 **AIDE_MEMOIRE.md** : Aide-mémoire ultra-rapide (30 sec)

### Documentation officielle

- 🌐 [Flyway Documentation](https://flywaydb.org/documentation/)
- 🌐 [SQL-based migrations](https://flywaydb.org/documentation/concepts/migrations#sql-based-migrations)

---

## 🎉 Résumé

✅ **Simple** : Juste des fichiers SQL  
✅ **Automatique** : Flyway s'occupe de l'ordre et du versioning  
✅ **Robuste** : Détection des changements (checksum)  
✅ **Production-ready** : Historique complet dans `flyway_schema_history`  
✅ **Flexible** : Toute requête SQL est supportée  

---

**Date de dernière mise à jour** : 5 mars 2026  
**Auteur** : Équipe PayToGether

