# ⚠️ IMPORTANT : Exécuter manuellement ce script SQL

## 📋 Instructions

### Option 1 : Via psql (ligne de commande)
```bash
PGPASSWORD='ZhKdrpgibRkzWodAw6rRABraPdXRcbpW' psql \
  -h 31.97.132.132 \
  -p 5432 \
  -U postgres \
  -d postgres \
  -f db/doc/INIT_flyway_schema_history.sql
```

### Option 2 : Via DBeaver, pgAdmin, ou autre client SQL
1. Se connecter à la base de données :
   - Host: `31.97.132.132`
   - Port: `5432`
   - Database: `postgres`
   - User: `postgres`
   - Password: `ZhKdrpgibRkzWodAw6rRABraPdXRcbpW`

2. Copier-coller le contenu de `INIT_flyway_schema_history.sql`

3. Exécuter le script

### Option 3 : Via IntelliJ IDEA Database Tool
1. Ouvrir Database Tool (View → Tool Windows → Database)
2. Ajouter une connexion PostgreSQL
3. Ouvrir `INIT_flyway_schema_history.sql`
4. Clic droit → Execute

---

## ✅ Résultat attendu

Après exécution, vous devriez voir :
```
CREATE TABLE
INSERT 0 4
 installed_rank | version |        description        |     installed_on      | success 
----------------+---------+---------------------------+-----------------------+---------
              1 | 1       | schema initial            | 2026-03-06 22:45:00   | t
              2 | 2       | ajout table paiement      | 2026-03-06 22:45:00   | t
              3 | 3       | ajout telephone utilisat… | 2026-03-06 22:45:00   | t
              4 | 4       | ajout tables manquantes   | 2026-03-06 22:45:00   | t
(4 rows)
```

---

## 🚀 Après l'exécution

Une fois le script exécuté avec succès :
1. ✅ Flyway sera réactivé automatiquement (voir configuration ci-dessous)
2. ✅ Les migrations V1, V2, V3, V4 seront ignorées (déjà marquées comme appliquées)
3. ✅ Les nouvelles migrations (V5+) seront automatiquement appliquées au démarrage

---

## 📝 Fichier concerné
`modules/bff/bff-configuration/src/main/resources/db/doc/INIT_flyway_schema_history.sql`

