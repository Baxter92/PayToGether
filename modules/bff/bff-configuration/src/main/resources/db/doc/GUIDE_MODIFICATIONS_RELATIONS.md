# Guide : Modifications de relations JPA avec Flyway
## 🎯 Objectif
Ce guide vous montre comment modifier les relations entre entités (OneToOne, ManyToOne, ManyToMany) en utilisant des migrations Flyway SQL.
---
## 📚 10 cas d'usage couverts
📄 **Voir `EXEMPLES__modifications_relations.sql` pour tous les exemples détaillés basés sur vos entités JPA réelles**
### 1. OneToOne → ManyToOne
**Exemple** : Commande (un marchand → plusieurs commandes)
- Supprimer la contrainte d'unicité
- Garder la FK existante
### 2. ManyToOne → OneToOne
**Exemple** : Adresse (plusieurs adresses → une adresse par utilisateur)
- Vérifier les doublons
- Ajouter une contrainte d'unicité
### 3. Supprimer une relation ManyToMany
**Exemple** : Deal participants (supprimer la relation)
- DROP TABLE de jointure
### 4. Créer une relation ManyToMany
**Exemple** : Utilisateur favoris (créer nouvelle relation)
- CREATE TABLE de jointure avec clé primaire composée
### 5. Modifier une table de jointure
**Exemple** : Ajouter date_inscription et statut
- ALTER TABLE ADD COLUMN
### 6. Changer ON DELETE
**Exemple** : Categorie (CASCADE → RESTRICT)
- DROP CONSTRAINT + ADD CONSTRAINT
### 7. Ajouter une FK optionnelle
**Exemple** : Utilisateur parrain (auto-référencée)
- ADD COLUMN nullable + FK
### 8. Supprimer une FK
**Exemple** : Supprimer colonne "note" de Commentaire
- DROP CONSTRAINT + DROP COLUMN
### 9. Renommer une FK
**Exemple** : utilisateur_uuid → marchand_uuid dans Deal
- DROP CONSTRAINT + RENAME COLUMN + ADD CONSTRAINT
### 10. Relation auto-référencée
**Exemple** : Commentaire parent (hiérarchie de commentaires)
- FK vers la même table
---
## 🔑 Pattern général pour modifier une FK
```sql
-- 1. Supprimer la FK existante
ALTER TABLE {table_enfant} DROP CONSTRAINT IF EXISTS fk_{nom_relation};
-- 2. Modifier la colonne (optionnel)
ALTER TABLE {table_enfant} RENAME COLUMN {ancienne} TO {nouvelle};
ALTER TABLE {table_enfant} ALTER COLUMN {colonne} SET NOT NULL;
-- 3. Recréer la FK
ALTER TABLE {table_enfant} 
ADD CONSTRAINT fk_{nouveau_nom} 
    FOREIGN KEY ({colonne}) 
    REFERENCES {table_parent}(uuid) 
    ON DELETE {CASCADE|RESTRICT|SET NULL};
-- 4. Gérer les index
DROP INDEX IF EXISTS idx_{ancien};
CREATE INDEX idx_{nouveau} ON {table_enfant}({coloCREATE INDE---
## 🛡️ Stratégies ON DELETE
| Stratégie | Comportement | Exemple |
|-----------|-------------|---------|
| **CASCADE** | Supprime les enfants si le parent est supprimé | Supprimer un deal supprime ses images |
| **RESTRICT** | Empêche la suppressi| **RESTRICT** | Empêche la suppent | Empêche de supprimer une catégorie si des deals l'utilisen| **| **SET NULL** | Met la F| **RESTRICT** | Empêche la suppressi|Si un parrain est supprimé, parrain_uuid devient NULL |
| **NO ACTION** | Similaire à RESTRICT (défaut) | |
---
## ✅ Checklist avant modification
- [ ] Vérifier l'entité JPA correspondante (annotations @OneToOne, @ManyToOne, @ManyToMany)
- [ ] Identifier la colonne FK en base (ex: utilisateur_uuid, deal_uuid)
- [ ] Identifier la table de jointure (pour ManyToMany)
- [ ] Vérifier les contraintes existantes (UNIQUE, FK)
- [ ] Déterminer la stratégie ON DELETE souhaitée
- [ ] Tester localement avant de pousser
---
## 📄 Exemples complets
**Tous les exemples SQL détaillés avec explications** :  
→ `EXEMPLES__modifi→ `EXEMPLES__modifi→ `EXEMPLE à copier** :  
→ `TEMPLATE__migration.sql`
**Guide rapide** :  
→ `GUIDE_RAPIDE.md`
---
**Date de dernière mise à jour** : 6 mars 2026  
**Auteur** : Équipe PayToGether
