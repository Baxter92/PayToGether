# 🔗 Guide Visuel : Modifications de Relations JPA

## 📊 Tableau récapitulatif

| Type de modification | Difficulté | Nécessite migration données | Fichier exemple |
|----------------------|------------|----------------------------|-----------------|
| Renommer colonne FK | ⭐ Facile | Non | EXEMPLE__modification_relation.sql |
| OneToOne → ManyToOne | ⭐⭐ Moyen | Non | EXEMPLES (Cas 1) |
| ManyToOne → OneToOne | ⭐⭐⭐ Difficile | Oui (doublons) | EXEMPLES (Cas 2) |
| Créer ManyToMany | ⭐⭐ Moyen | Non | EXEMPLES (Cas 4) |
| Supprimer ManyToMany | ⭐ Facile | Peut-être | EXEMPLES (Cas 3) |
| Changer ON DELETE | ⭐ Facile | Non | EXEMPLES (Cas 6) |
| Ajouter FK optionnelle | ⭐ Facile | Non | EXEMPLES (Cas 7) |
| Supprimer FK | ⭐ Facile | Peut-être | EXEMPLES (Cas 8) |

---

## 🎯 Cas d'usage les plus fréquents

### 1️⃣ OneToOne → ManyToOne (⭐⭐ Moyen)

**Besoin** : Un utilisateur avait UNE adresse, maintenant il peut en avoir PLUSIEURS.

**Avant (JPA)** :
```java
@Entity
public class Utilisateur {
    @OneToOne
    @JoinColumn(name = "adresse_uuid")
    private Adresse adresse;
}
```

**Après (JPA)** :
```java
@Entity
public class Utilisateur {
    @OneToMany(mappedBy = "utilisateur")
    private List<Adresse> adresses;
}

@Entity
public class Adresse {
    @ManyToOne
    @JoinColumn(name = "utilisateur_uuid")
    private Utilisateur utilisateur;
    
    private Boolean estPrincipale;  // ✅ Nouvelle colonne
}
```

**Migration SQL** :
```sql
-- V4__onetoone_vers_manytoone.sql

-- 1. Supprimer la contrainte UNIQUE (qui empêchait les doublons)
ALTER TABLE adresse 
DROP CONSTRAINT IF EXISTS uk_adresse_utilisateur;

-- 2. Ajouter une colonne pour identifier l'adresse principale
ALTER TABLE adresse 
ADD COLUMN IF NOT EXISTS est_principale BOOLEAN DEFAULT FALSE;

-- 3. Mettre à jour les données existantes
UPDATE adresse SET est_principale = TRUE;

-- 4. Index
CREATE INDEX IF NOT EXISTS idx_adresse_est_principale ON adresse(est_principale);
```

**✅ Résultat** : Un utilisateur peut avoir plusieurs adresses, avec une adresse principale.

---

### 2️⃣ ManyToOne → OneToOne (⭐⭐⭐ Difficile)

**Besoin** : Un utilisateur avait PLUSIEURS adresses, maintenant il ne peut en avoir qu'UNE.

**⚠️ ATTENTION** : Nécessite de supprimer les adresses en double !

**Migration SQL** :
```sql
-- V5__manytoone_vers_onetoone.sql

-- 1. Supprimer les adresses secondaires (garder l'adresse principale)
DELETE FROM adresse 
WHERE est_principale = FALSE;

-- 2. Ajouter une contrainte UNIQUE
ALTER TABLE adresse 
ADD CONSTRAINT uk_adresse_utilisateur UNIQUE (utilisateur_uuid);

-- 3. Supprimer la colonne est_principale
ALTER TABLE adresse 
DROP COLUMN IF EXISTS est_principale;
```

**✅ Résultat** : Un utilisateur a une seule adresse (les autres sont supprimées).

---

### 3️⃣ Créer une relation ManyToMany (⭐⭐ Moyen)

**Besoin** : Un deal peut avoir plusieurs tags, un tag peut être sur plusieurs deals.

**Structure (JPA)** :
```java
@Entity
public class Deal {
    @ManyToMany
    @JoinTable(
        name = "deal_tag",
        joinColumns = @JoinColumn(name = "deal_uuid"),
        inverseJoinColumns = @JoinColumn(name = "tag_uuid")
    )
    private Set<Tag> tags;
}

@Entity
public class Tag {
    @ManyToMany(mappedBy = "tags")
    private Set<Deal> deals;
}
```

**Migration SQL** :
```sql
-- V6__creation_manytomany_deal_tag.sql

-- 1. Créer la table Tag
CREATE TABLE IF NOT EXISTS tag (
    uuid UUID PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. Créer la table de jointure
CREATE TABLE IF NOT EXISTS deal_tag (
    deal_uuid UUID NOT NULL,
    tag_uuid UUID NOT NULL,
    
    PRIMARY KEY (deal_uuid, tag_uuid),
    
    CONSTRAINT fk_deal_tag_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_deal_tag_tag FOREIGN KEY (tag_uuid)
        REFERENCES tag(uuid) ON DELETE CASCADE
);

-- 3. Index
CREATE INDEX idx_deal_tag_deal_uuid ON deal_tag(deal_uuid);
CREATE INDEX idx_deal_tag_tag_uuid ON deal_tag(tag_uuid);
```

**✅ Résultat** : Relation ManyToMany avec table de jointure.

---

### 4️⃣ Changer ON DELETE (⭐ Facile)

**Besoin** : Empêcher de supprimer une catégorie qui a des deals.

**Avant** :
```sql
FOREIGN KEY (categorie_uuid) REFERENCES categorie(uuid) ON DELETE CASCADE
```
→ Supprimer une catégorie supprime tous ses deals ❌

**Après** :
```sql
FOREIGN KEY (categorie_uuid) REFERENCES categorie(uuid) ON DELETE RESTRICT
```
→ Impossible de supprimer une catégorie qui a des deals ✅

**Migration SQL** :
```sql
-- V7__modification_on_delete_categorie.sql

-- 1. Supprimer la contrainte
ALTER TABLE deal 
DROP CONSTRAINT IF EXISTS fk_deal_categorie;

-- 2. Recréer avec ON DELETE RESTRICT
ALTER TABLE deal 
ADD CONSTRAINT fk_deal_categorie 
    FOREIGN KEY (categorie_uuid) 
    REFERENCES categorie(uuid) 
    ON DELETE RESTRICT;
```

**✅ Résultat** : Protection contre la suppression accidentelle.

---

### 5️⃣ Ajouter une FK optionnelle (⭐ Facile)

**Besoin** : Un utilisateur peut avoir un parrain (relation auto-référencée).

**Structure (JPA)** :
```java
@Entity
public class Utilisateur {
    @ManyToOne
    @JoinColumn(name = "parrain_uuid")
    private Utilisateur parrain;  // Optionnel
    
    @OneToMany(mappedBy = "parrain")
    private List<Utilisateur> filleuls;
}
```

**Migration SQL** :
```sql
-- V8__ajout_parrainage.sql

-- 1. Ajouter la colonne (nullable)
ALTER TABLE utilisateur 
ADD COLUMN IF NOT EXISTS parrain_uuid UUID;

-- 2. Ajouter la FK
ALTER TABLE utilisateur 
ADD CONSTRAINT fk_utilisateur_parrain 
    FOREIGN KEY (parrain_uuid) 
    REFERENCES utilisateur(uuid) 
    ON DELETE SET NULL;

-- 3. Index
CREATE INDEX idx_utilisateur_parrain ON utilisateur(parrain_uuid);
```

**✅ Résultat** : Système de parrainage avec relation auto-référencée.

---

## 🛠️ Patterns SQL réutilisables

### Pattern 1 : Renommer une colonne FK

```sql
-- 1. DROP constraint
ALTER TABLE ma_table DROP CONSTRAINT IF EXISTS fk_ancienne;

-- 2. RENAME column
ALTER TABLE ma_table RENAME COLUMN ancienne_col TO nouvelle_col;

-- 3. ADD constraint
ALTER TABLE ma_table ADD CONSTRAINT fk_nouvelle 
    FOREIGN KEY (nouvelle_col) REFERENCES autre_table(uuid);

-- 4. DROP + CREATE index
DROP INDEX IF EXISTS idx_ancienne_col;
CREATE INDEX idx_nouvelle_col ON ma_table(nouvelle_col);
```

### Pattern 2 : Ajouter une FK

```sql
-- 1. ADD column
ALTER TABLE ma_table ADD COLUMN IF NOT EXISTS autre_uuid UUID;

-- 2. ADD constraint
ALTER TABLE ma_table ADD CONSTRAINT fk_ma_table_autre 
    FOREIGN KEY (autre_uuid) REFERENCES autre_table(uuid);

-- 3. CREATE index
CREATE INDEX idx_ma_table_autre ON ma_table(autre_uuid);
```

### Pattern 3 : Supprimer une FK

```sql
-- 1. DROP constraint
ALTER TABLE ma_table DROP CONSTRAINT IF EXISTS fk_ma_table_autre;

-- 2. DROP index
DROP INDEX IF EXISTS idx_ma_table_autre;

-- 3. DROP column
ALTER TABLE ma_table DROP COLUMN IF EXISTS autre_uuid;
```

---

## 📚 Tous les exemples détaillés

📄 **EXEMPLES__modifications_relations.sql** contient :

1. ✅ OneToOne → ManyToOne
2. ✅ ManyToOne → OneToOne
3. ✅ Supprimer ManyToMany
4. ✅ Créer ManyToMany
5. ✅ Modifier table de jointure
6. ✅ Changer ON DELETE
7. ✅ Ajouter FK optionnelle
8. ✅ Supprimer FK
9. ✅ FK composite
10. ✅ Relation bidirectionnelle

---

## ⚠️ Checklist avant modification

- [ ] Sauvegarder les données si nécessaire
- [ ] Identifier les doublons potentiels
- [ ] Planifier la stratégie de nettoyage
- [ ] Tester la migration sur un environnement de dev
- [ ] Mettre à jour les entités JPA correspondantes
- [ ] Mettre à jour les repositories/services si nécessaire
- [ ] Tester les requêtes après migration

---

**Tout est documenté dans `EXEMPLES__modifications_relations.sql` !** 🚀

