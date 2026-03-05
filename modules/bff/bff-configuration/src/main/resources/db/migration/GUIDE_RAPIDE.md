# 🚀 Guide Rapide : Migrations SQL avec Flyway

## 📝 Ajouter une migration (2 étapes !)

### 1️⃣ Créer le fichier SQL

**Nom** : `V{N}__{description_en_snake_case}.sql`

**Exemples** :
- `V4__ajout_table_adresse.sql`
- `V5__modification_colonne_email.sql`
- `V6__ajout_index_performance.sql`

**Contenu** :
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
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_adresse_utilisateur 
        FOREIGN KEY (utilisateur_uuid) 
        REFERENCES utilisateur(uuid) 
        ON DELETE CASCADE
);

COMMENT ON TABLE adresse IS 'Adresses des utilisateurs';
CREATE INDEX idx_adresse_utilisateur ON adresse(utilisateur_uuid);
```

### 2️⃣ Redémarrer l'application

```bash
mvn spring-boot:run
```

✅ **C'est tout !** Flyway applique automatiquement la migration.

---

## 🔄 Modifier une relation

**Exemple** : Renommer `createur_uuid` → `proprietaire_uuid`

```sql
-- V7__renommage_createur_proprietaire.sql

-- 1. Supprimer la FK
ALTER TABLE deal DROP CONSTRAINT IF EXISTS fk_deal_createur;

-- 2. Renommer la colonne
ALTER TABLE deal RENAME COLUMN createur_uuid TO proprietaire_uuid;

-- 3. Recréer la FK
ALTER TABLE deal 
ADD CONSTRAINT fk_deal_proprietaire 
    FOREIGN KEY (proprietaire_uuid) 
    REFERENCES utilisateur(uuid) 
    ON DELETE CASCADE;

-- 4. Index
DROP INDEX IF EXISTS idx_deal_createur_uuid;
CREATE INDEX idx_deal_proprietaire_uuid ON deal(proprietaire_uuid);
```

📄 **Voir exemple complet** : `EXEMPLE__modification_relation.sql`

---

## 🔗 Modifier le type de relation (OneToOne ↔ ManyToOne, ManyToMany, etc.)

**10 cas d'usage avec exemples complets** :

1. **OneToOne → ManyToOne** (Une adresse → Plusieurs adresses)
2. **ManyToOne → OneToOne** (Plusieurs adresses → Une adresse)
3. **Supprimer une relation ManyToMany** (avec table de jointure)
4. **Créer une relation ManyToMany** (avec table de jointure)
5. **Modifier une table de jointure** (ajouter des colonnes)
6. **Changer ON DELETE** (CASCADE → RESTRICT)
7. **Ajouter une FK optionnelle** (nullable)
8. **Supprimer une FK** (et la colonne)
9. **FK composite** (clé primaire composée)
10. **Relation bidirectionnelle** (owner + inverse)

📄 **Voir tous les exemples** : `EXEMPLES__modifications_relations.sql`

---

## 📋 Conventions de nommage Flyway

### Format obligatoire
`V{VERSION}__{description}.sql`

**VERSION** : Nombre entier croissant (1, 2, 3, ...)  
**description** : En snake_case (mots séparés par `_`)

### ✅ Valides
- `V1__schema_initial.sql`
- `V2__ajout_table_paiement.sql`
- `V10__modification_majeure.sql`
- `V100__refonte_complete.sql`

### ❌ Invalides
- `V1.0.0__schema.sql` (pas de points dans la version)
- `V2-ajout-table.sql` (utiliser `__` et `_`)
- `migration_1.sql` (doit commencer par `V`)

---

## 🛡️ Bonnes pratiques

1. ✅ **Toujours** utiliser `IF EXISTS` / `IF NOT EXISTS`
2. ✅ **Toujours** ajouter `COMMENT ON TABLE/COLUMN`
3. ✅ **Toujours** créer des index sur les FK
4. ✅ **Toujours** définir `ON DELETE` (CASCADE, RESTRICT, SET NULL)
5. ❌ **Jamais** modifier une migration déjà appliquée (créer une nouvelle)

---

## 📚 Templates disponibles

📄 **TEMPLATE__migration.sql** → Template à copier  
📄 **EXEMPLE__modification_relation.sql** → Exemple renommage FK  
📄 **EXEMPLES__modifications_relations.sql** → 10 cas de modifications de relations (OneToOne, ManyToOne, ManyToMany, etc.)

---

## 🔍 Vérifier l'historique

Flyway crée une table `flyway_schema_history` :

```sql
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

---

## ⚡ Workflow complet

1. **Copier** le template : `cp TEMPLATE__migration.sql V4__ma_migration.sql`
2. **Éditer** le fichier avec ton SQL
3. **Redémarrer** l'application
4. ✅ **Vérifier** les logs : `"Migrating schema ... to V4"`

---

✅ **Simple, rapide, efficace !**

