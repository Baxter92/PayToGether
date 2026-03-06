# 🚀 Guide Rapide : Ajouter une Migration SQL

## 📝 Étapes (5 minutes)

### 1. Créer le script SQL

**Fichier** : `db/changelog/sql/V{VERSION}__{description}.sql`

```sql
-- =========================================================================
-- Version: 1.X.X
-- Date: 2026-XX-XX
-- Auteur: Ton nom
-- Description: Ce que fait la migration
-- =========================================================================

-- Ton SQL ici
CREATE TABLE IF NOT EXISTS ma_table (...);

-- Commentaires
COMMENT ON TABLE ma_table IS 'Description';
```

---

### 2. Créer le fichier XML

**Fichier** : `db/changelog/versions/v{VERSION}-{description}.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
    <changeSet id="v1.X.X-ma-migration" author="ton-nom">
        <comment>Description</comment>
        <sqlFile path="db/changelog/sql/V1.X.X__ma_migration.sql"
                 relativeToChangelogFile="false"
                 splitStatements="true"
                 stripComments="false"/>
        <rollback>
            DROP TABLE IF EXISTS ma_table CASCADE;
        </rollback>
    </changeSet>
</databaseChangeLog>
```

---

### 3. Ajouter l'include

**Fichier** : `db/changelog/db.changelog-master.xml`

```xml
<!-- Ajouter à la fin, avant la balise fermante -->
<include file="db/changelog/versions/v1.X.X-ma-migration.xml"/>
```

---

### 4. Tester

```bash
mvn spring-boot:run
# ✅ Vérifie les logs: "Liquibase Update Successful"
```

---

## 🔄 Modifier une Relation (Exemple)

```sql
-- 1. Supprimer la contrainte
ALTER TABLE deal DROP CONSTRAINT IF EXISTS fk_deal_createur;

-- 2. Renommer la colonne
ALTER TABLE deal RENAME COLUMN createur_uuid TO proprietaire_uuid;

-- 3. Recréer la contrainte
ALTER TABLE deal 
ADD CONSTRAINT fk_deal_proprietaire 
    FOREIGN KEY (proprietaire_uuid) 
    REFERENCES utilisateur(uuid) 
    ON DELETE CASCADE;

-- 4. Mettre à jour les index
DROP INDEX IF EXISTS idx_deal_createur_uuid;
CREATE INDEX idx_deal_proprietaire_uuid ON deal(proprietaire_uuid);
```

---

## 📚 Documentation Complète

📄 **README_MIGRATIONS_SQL.md** → Guide complet avec exemples

📄 **TEMPLATE_migration.sql** → Template SQL

📄 **TEMPLATE_migration_sql.xml** → Template XML

📄 **EXEMPLE_modification_relation.sql** → Exemple concret

---

✅ **C'est tout ! Simple et efficace.**

