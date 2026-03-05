# 📚 Guide de Gestion des Migrations SQL avec Liquibase

## 🎯 Principe

Ce projet utilise **Liquibase** pour exécuter des **scripts SQL purs**. Tu as le contrôle total sur tes migrations SQL, et Liquibase s'occupe uniquement du versioning et de l'exécution.

---

## 📁 Structure

```
db/changelog/
├── sql/                                    # Scripts SQL purs
│   ├── V1.0.0__schema_initial.sql
│   ├── V1.1.0__ajout_table_paiement.sql
│   ├── V1.2.0__ajout_telephone_utilisateur.sql
│   ├── TEMPLATE_migration.sql              # Template pour nouvelles migrations
│   └── EXEMPLE_modification_relation.sql   # Exemple de modification de relation
│
├── versions/                               # Fichiers XML Liquibase (référencent les SQL)
│   ├── v1.0.0-schema-initial-sql.xml
│   ├── v1.1.0-ajout-table-paiement.xml
│   ├── v1.2.0-ajout-telephone-utilisateur.xml
│   └── TEMPLATE_migration_sql.xml
│
└── db.changelog-master.xml                 # Fichier maître (liste toutes les migrations)
```

---

## ✅ Checklist pour créer une nouvelle migration

### 1️⃣ Créer le script SQL

**Fichier** : `db/changelog/sql/V{VERSION}__{description}.sql`

**Exemple** : `V1.3.0__ajout_table_adresse.sql`

```sql
-- =========================================================================
-- Version: 1.3.0
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

CREATE INDEX idx_adresse_utilisateur_uuid ON adresse(utilisateur_uuid);
```

---

### 2️⃣ Créer le fichier XML Liquibase

**Fichier** : `db/changelog/versions/v{VERSION}-{description}.xml`

**Exemple** : `v1.3.0-ajout-table-adresse.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="v1.3.0-ajout-table-adresse" author="ton-nom">
        <comment>Ajout de la table adresse pour les utilisateurs</comment>
        <sqlFile 
            path="db/changelog/sql/V1.3.0__ajout_table_adresse.sql"
            relativeToChangelogFile="false"
            splitStatements="true"
            stripComments="false"/>
        <rollback>
            DROP TABLE IF EXISTS adresse CASCADE;
        </rollback>
    </changeSet>

</databaseChangeLog>
```

---

### 3️⃣ Ajouter l'include dans le fichier maître

**Fichier** : `db/changelog/db.changelog-master.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>

    <!-- Schéma initial -->
    <include file="db/changelog/versions/v1.0.0-schema-initial-sql.xml"/>
    
    <!-- Table paiement -->
    <include file="db/changelog/versions/v1.1.0-ajout-table-paiement.xml"/>
    
    <!-- Téléphone utilisateur -->
    <include file="db/changelog/versions/v1.2.0-ajout-telephone-utilisateur.xml"/>
    
    <!-- ✅ AJOUTER ICI LE NOUVEAU FICHIER -->
    <include file="db/changelog/versions/v1.3.0-ajout-table-adresse.xml"/>

</databaseChangeLog>
```

---

### 4️⃣ Tester la migration

```bash
# Démarrer l'application (Liquibase s'exécute au démarrage)
mvn spring-boot:run

# Vérifier les logs
# ✅ Tu devrais voir: "Liquibase Update Successful"
```

---

## 🔄 Cas d'usage : Modifier une relation existante

### Exemple : Renommer une colonne avec clé étrangère

**Fichier** : `V1.4.0__renommage_createur_proprietaire.sql`

```sql
-- =========================================================================
-- Version: 1.4.0
-- Date: 2026-03-05
-- Auteur: Ton nom
-- Description: Renommage de createur_uuid en proprietaire_uuid
-- =========================================================================

-- ÉTAPE 1: Supprimer la contrainte de clé étrangère
ALTER TABLE deal 
DROP CONSTRAINT IF EXISTS fk_deal_createur;

-- ÉTAPE 2: Renommer la colonne
ALTER TABLE deal 
RENAME COLUMN createur_uuid TO proprietaire_uuid;

-- ÉTAPE 3: Recréer la contrainte avec le nouveau nom
ALTER TABLE deal 
ADD CONSTRAINT fk_deal_proprietaire 
    FOREIGN KEY (proprietaire_uuid) 
    REFERENCES utilisateur(uuid) 
    ON DELETE CASCADE;

-- ÉTAPE 4: Mettre à jour les index
DROP INDEX IF EXISTS idx_deal_createur_uuid;
CREATE INDEX idx_deal_proprietaire_uuid ON deal(proprietaire_uuid);

-- ÉTAPE 5: Commentaires
COMMENT ON COLUMN deal.proprietaire_uuid IS 'UUID du propriétaire du deal';
```

---

## 📋 Conventions de nommage

### Scripts SQL
- **Format** : `V{MAJOR}.{MINOR}.{PATCH}__{description_en_snake_case}.sql`
- **Exemples** :
  - `V1.0.0__schema_initial.sql`
  - `V1.1.0__ajout_table_paiement.sql`
  - `V1.2.0__ajout_telephone_utilisateur.sql`
  - `V2.0.0__refonte_complete.sql`

### Fichiers XML
- **Format** : `v{version}-{description-en-kebab-case}.xml`
- **Exemples** :
  - `v1.0.0-schema-initial-sql.xml`
  - `v1.1.0-ajout-table-paiement.xml`
  - `v1.2.0-ajout-telephone-utilisateur.xml`

### ChangeSet ID
- **Format** : `v{version}-{description-en-kebab-case}`
- **Exemple** : `v1.3.0-ajout-table-adresse`

---

## 🛡️ Bonnes pratiques SQL

### 1. Toujours utiliser IF EXISTS / IF NOT EXISTS
```sql
-- ✅ BON
CREATE TABLE IF NOT EXISTS ma_table (...);
ALTER TABLE ma_table ADD COLUMN IF NOT EXISTS ma_colonne VARCHAR(100);
DROP TABLE IF EXISTS ma_table CASCADE;

-- ❌ MAUVAIS (échoue si déjà exécuté)
CREATE TABLE ma_table (...);
ALTER TABLE ma_table ADD COLUMN ma_colonne VARCHAR(100);
```

### 2. Définir les contraintes ON DELETE
```sql
-- ✅ BON
CONSTRAINT fk_deal_utilisateur 
    FOREIGN KEY (utilisateur_uuid) 
    REFERENCES utilisateur(uuid) 
    ON DELETE CASCADE;  -- Supprime les deals si utilisateur supprimé

CONSTRAINT fk_deal_categorie 
    FOREIGN KEY (categorie_uuid) 
    REFERENCES categorie(uuid) 
    ON DELETE RESTRICT;  -- Empêche de supprimer une catégorie utilisée
```

### 3. Ajouter des commentaires
```sql
COMMENT ON TABLE deal IS 'Deals créés par les utilisateurs';
COMMENT ON COLUMN deal.statut IS 'Statut: BROUILLON, PUBLIE, EXPIRE, ANNULE';
```

### 4. Créer des index
```sql
-- Index sur clés étrangères (performances)
CREATE INDEX IF NOT EXISTS idx_deal_createur_uuid ON deal(createur_uuid);
CREATE INDEX IF NOT EXISTS idx_deal_categorie_uuid ON deal(categorie_uuid);

-- Index sur colonnes fréquemment filtrées
CREATE INDEX IF NOT EXISTS idx_deal_statut ON deal(statut);
CREATE INDEX IF NOT EXISTS idx_deal_date_fin ON deal(date_fin);
```

### 5. Utiliser des valeurs par défaut
```sql
-- ✅ BON
statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
est_principale BOOLEAN DEFAULT FALSE
```

---

## 🚨 Modifier une migration existante

### ⚠️ ATTENTION : Ne JAMAIS modifier un script déjà appliqué en production !

**Si la migration est déjà appliquée** :
1. ✅ Créer une NOUVELLE migration corrective
2. ❌ Ne PAS modifier le fichier existant

**Exemple** :
```sql
-- V1.3.1__correction_table_adresse.sql
ALTER TABLE adresse 
ADD COLUMN IF NOT EXISTS numero_appartement VARCHAR(20);
```

---

## 🔍 Vérifier l'état des migrations

Liquibase crée une table `databasechangelog` qui contient l'historique :

```sql
-- Voir toutes les migrations appliquées
SELECT id, author, filename, dateexecuted, orderexecuted 
FROM databasechangelog 
ORDER BY orderexecuted;
```

---

## 📝 Templates disponibles

### Template SQL
📄 `db/changelog/sql/TEMPLATE_migration.sql`

### Template XML
📄 `db/changelog/versions/TEMPLATE_migration_sql.xml`

### Exemple de modification de relation
📄 `db/changelog/sql/EXEMPLE_modification_relation.sql`

---

## 🎯 Résumé rapide

1. **Créer le script SQL** dans `db/changelog/sql/`
2. **Créer le fichier XML** dans `db/changelog/versions/`
3. **Ajouter l'include** dans `db.changelog-master.xml`
4. **Démarrer l'application** → Liquibase s'exécute automatiquement

✅ **Simple, prévisible, contrôlé !**

---

**Date de dernière mise à jour** : 5 mars 2026  
**Auteur** : Équipe PayToGether

