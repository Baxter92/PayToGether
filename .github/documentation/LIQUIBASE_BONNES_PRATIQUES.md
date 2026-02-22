# Bonnes pratiques Liquibase - PayToGether

## 📋 Règles d'or pour PayToGether

### 1. Convention de nommage des changesets

**Format de l'ID** : `{version}-{action}-{objet}`

Exemples :
- `1.0.0-create-table-utilisateur`
- `1.1.0-add-column-deal-code-promo`
- `1.2.0-create-index-deal-date-creation`
- `2.0.0-alter-table-utilisateur-email`

**Actions standards** :
- `create-table-{nom}` : Création de table
- `add-column-{table}-{colonne}` : Ajout de colonne
- `modify-column-{table}-{colonne}` : Modification de colonne
- `drop-column-{table}-{colonne}` : Suppression de colonne
- `create-index-{table}-{colonne}` : Création d'index
- `add-fk-{table1}-{table2}` : Ajout de clé étrangère
- `insert-data-{table}` : Insertion de données
- `tag-release` : Tag de version

### 2. Architecture hexagonale & Liquibase

Lors de la création d'une nouvelle entité selon l'architecture du projet :

#### Étape 1 : Créer l'entité JPA
```java
// bff-provider/adapter/entity/CommandeJpa.java
@Entity
@Table(name = "commande")
public class CommandeJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    // ...
}
```

#### Étape 2 : Créer le changelog Liquibase
```xml
<!-- v1.x.x-create-table-commande.xml -->
<changeSet id="1.x.x-create-table-commande" author="votre-nom">
    <createTable tableName="commande">
        <column name="uuid" type="UUID">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <!-- colonnes selon l'entité JPA -->
    </createTable>
</changeSet>
```

#### Étape 3 : Synchroniser
- L'entité JPA définit le **modèle objet**
- Le changelog Liquibase définit le **schéma relationnel**
- Ils doivent être **toujours synchronisés**

### 3. Types de données standards

Correspondance JPA ↔ Liquibase pour PayToGether :

| Type Java | Annotation JPA | Type Liquibase | Exemple |
|-----------|---------------|----------------|---------|
| `UUID` | `@Id` | `UUID` | `uuid` |
| `String` | `@Column(length=100)` | `VARCHAR(100)` | `nom` |
| `String` | `@Column(columnDefinition="TEXT")` | `TEXT` | `description` |
| `LocalDateTime` | `@Column` | `TIMESTAMP` | `date_creation` |
| `BigDecimal` | `@Column(precision=10, scale=2)` | `DECIMAL(10,2)` | `prix_deal` |
| `Integer` | `@Column` | `INTEGER` | `nb_participants` |
| `Boolean` | `@Column` | `BOOLEAN` | `active` |
| `Enum` | `@Enumerated(EnumType.STRING)` | `VARCHAR(50)` | `statut` |

### 4. Gestion des images MinIO

Pour toute table d'images, respecter cette structure :

```xml
<createTable tableName="image_{entite}">
    <column name="uuid" type="UUID">
        <constraints primaryKey="true" nullable="false"/>
    </column>
    <column name="url_image" type="VARCHAR(500)">
        <constraints nullable="false" unique="true"/>
    </column>
    <column name="{entite}_uuid" type="UUID">
        <constraints nullable="false"/>
    </column>
    <column name="statut" type="VARCHAR(50)">
        <constraints nullable="false"/>
    </column>
    <column name="date_creation" type="TIMESTAMP">
        <constraints nullable="false"/>
    </column>
    <column name="date_modification" type="TIMESTAMP">
        <constraints nullable="false"/>
    </column>
</createTable>

<addDefaultValue tableName="image_{entite}" 
                 columnName="statut" 
                 defaultValue="PENDING"/>

<addForeignKeyConstraint
        baseTableName="image_{entite}"
        baseColumnNames="{entite}_uuid"
        referencedTableName="{entite}"
        referencedColumnNames="uuid"
        constraintName="fk_image_{entite}_{entite}"
        onDelete="CASCADE"/>
```

### 5. Gestion des timestamps

**Toujours inclure** dans chaque table :

```xml
<column name="date_creation" type="TIMESTAMP">
    <constraints nullable="false"/>
</column>
<column name="date_modification" type="TIMESTAMP">
    <constraints nullable="false"/>
</column>
```

Correspondance avec les annotations JPA :
```java
@CreationTimestamp
@Column(nullable = false, updatable = false)
private LocalDateTime dateCreation;

@UpdateTimestamp
@Column(nullable = false)
private LocalDateTime dateModification;
```

### 6. Gestion des clés étrangères

**Règle** : Toujours définir `onDelete` selon la logique métier

```xml
<!-- Suppression en cascade (si l'entité parente est supprimée) -->
<addForeignKeyConstraint
        constraintName="fk_image_deal_deal"
        onDelete="CASCADE"/>

<!-- Empêcher la suppression (si des enfants existent) -->
<addForeignKeyConstraint
        constraintName="fk_deal_categorie"
        onDelete="RESTRICT"/>

<!-- Mettre à NULL (relation optionnelle) -->
<addForeignKeyConstraint
        constraintName="fk_utilisateur_photo_profil"
        onDelete="SET NULL"/>
```

### 7. Index de performance

**Créer des index sur** :
- Colonnes utilisées dans les WHERE fréquents
- Colonnes utilisées dans les JOIN
- Colonnes utilisées pour le tri (ORDER BY)
- Colonnes de statut/état
- Colonnes de date (pour filtrage temporel)

```xml
<createIndex indexName="idx_{table}_{colonne}" tableName="{table}">
    <column name="{colonne}"/>
</createIndex>
```

**Index composites** (plusieurs colonnes) :
```xml
<createIndex indexName="idx_deal_statut_date" tableName="deal">
    <column name="statut"/>
    <column name="date_debut"/>
</createIndex>
```

### 8. Gestion des données de référence

#### Données obligatoires (production)
Placer dans `v1.0.0-donnees-initiales.xml` ou un fichier similaire **sans contexte** :

```xml
<changeSet id="1.0.0-insert-categories" author="paytogether">
    <insert tableName="categorie">
        <!-- Données requises en production -->
    </insert>
</changeSet>
```

#### Données de test (développement)
Placer dans `dev-donnees-test.xml` avec **context="dev"** :

```xml
<changeSet id="dev-insert-utilisateur-test" author="paytogether" context="dev">
    <insert tableName="utilisateur">
        <!-- Données de test uniquement -->
    </insert>
</changeSet>
```

### 9. Versioning

**Stratégie de versioning** :
- `v1.0.x` : Schéma initial et patches mineurs
- `v1.x.0` : Nouvelles fonctionnalités (ajout de tables/colonnes)
- `vx.0.0` : Changements majeurs (refactoring de schéma)

**Tags de release** :
Toujours taguer après un déploiement majeur :

```xml
<changeSet id="1.1.0-tag-release" author="paytogether">
    <tagDatabase tag="v1.1.0"/>
</changeSet>
```

### 10. Rollbacks

**Toujours prévoir un rollback** pour les changements critiques :

```xml
<changeSet id="1.1.0-add-column-deal-code-promo" author="paytogether">
    <addColumn tableName="deal">
        <column name="code_promo" type="VARCHAR(50)"/>
    </addColumn>
    
    <rollback>
        <dropColumn tableName="deal" columnName="code_promo"/>
    </rollback>
</changeSet>
```

**Types de rollback** :
- `<rollback>` : Rollback personnalisé
- `<rollback/>` : Rollback automatique (si supporté)
- Pas de rollback : Pour les données insérées (difficile à annuler)

### 11. Contextes par environnement

```xml
<!-- Tous les environnements (ou sans contexte) -->
<changeSet id="1.0.0-create-table-deal" author="paytogether">
    <!-- Schéma de base -->
</changeSet>

<!-- Développement uniquement -->
<changeSet id="dev-insert-test-data" author="paytogether" context="dev">
    <!-- Données de test -->
</changeSet>

<!-- Production uniquement -->
<changeSet id="prod-optimisation-index" author="paytogether" context="prod">
    <!-- Optimisations spécifiques production -->
</changeSet>

<!-- Dev ET prod (utiliser "common") -->
<changeSet id="common-insert-categories" author="paytogether" context="common">
    <!-- Données communes -->
</changeSet>
```

Configuration dans `application-{profil}.properties` :
```properties
# Dev
spring.liquibase.contexts=dev,common

# Prod
spring.liquibase.contexts=prod,common
```

### 12. Checklist avant commit

Avant de commiter un nouveau changelog :

- [ ] Le nom du fichier suit la convention `v{version}-{description}.xml`
- [ ] L'ID du changeset est unique et descriptif
- [ ] L'auteur est renseigné
- [ ] Un commentaire explique le changement
- [ ] Le changelog est inclus dans `db.changelog-master.xml`
- [ ] Les types de données correspondent aux entités JPA
- [ ] Les contraintes (NOT NULL, UNIQUE, FK) sont définies
- [ ] Les index de performance sont créés si nécessaire
- [ ] Un rollback est prévu si applicable
- [ ] Le changelog a été testé en local
- [ ] L'entité JPA correspondante est synchronisée

### 13. Workflow de développement

1. **Créer une branche** pour la fonctionnalité
2. **Créer l'entité JPA** dans `bff-provider/adapter/entity/`
3. **Créer le changelog Liquibase** correspondant
4. **Tester en local** avec base de données vide
5. **Vérifier le statut** : `./liquibase.sh status`
6. **Valider** : `./liquibase.sh validate`
7. **Commiter** entité JPA + changelog ensemble
8. **Review** : Un reviewer vérifie la cohérence JPA ↔ Liquibase
9. **Merge** : Fusionner dans la branche principale
10. **Déploiement** : Liquibase applique automatiquement au démarrage

### 14. Gestion des erreurs courantes

#### Erreur : Checksum invalide
**Cause** : Le changeset a été modifié après application

**Solution** :
```bash
# En développement uniquement :
./liquibase.sh clear-checksums

# En production : JAMAIS modifier un changeset appliqué
# Créer un nouveau changeset pour corriger
```

#### Erreur : Lock non libéré
**Cause** : L'application a crashé pendant une migration

**Solution** :
```sql
UPDATE databasechangeloglock SET locked = FALSE;
```

#### Erreur : Changeset déjà appliqué
**Cause** : Tentative de réappliquer un changeset

**Solution** : Vérifier `databasechangelog` et créer un nouveau changeset

### 15. Exemples complets

#### Exemple 1 : Ajouter une table avec relation

```xml
<changeSet id="1.1.0-create-table-commande" author="john.doe">
    <comment>Ajout de la table Commande pour gérer les achats groupés</comment>
    
    <!-- Création de la table -->
    <createTable tableName="commande">
        <column name="uuid" type="UUID">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="numero_commande" type="VARCHAR(50)">
            <constraints nullable="false" unique="true"/>
        </column>
        <column name="deal_uuid" type="UUID">
            <constraints nullable="false"/>
        </column>
        <column name="utilisateur_uuid" type="UUID">
            <constraints nullable="false"/>
        </column>
        <column name="statut" type="VARCHAR(50)">
            <constraints nullable="false"/>
        </column>
        <column name="montant_total" type="DECIMAL(10,2)">
            <constraints nullable="false"/>
        </column>
        <column name="date_creation" type="TIMESTAMP">
            <constraints nullable="false"/>
        </column>
        <column name="date_modification" type="TIMESTAMP">
            <constraints nullable="false"/>
        </column>
    </createTable>
    
    <!-- Clés étrangères -->
    <addForeignKeyConstraint
            baseTableName="commande"
            baseColumnNames="deal_uuid"
            referencedTableName="deal"
            referencedColumnNames="uuid"
            constraintName="fk_commande_deal"
            onDelete="RESTRICT"/>
    
    <addForeignKeyConstraint
            baseTableName="commande"
            baseColumnNames="utilisateur_uuid"
            referencedTableName="utilisateur"
            referencedColumnNames="uuid"
            constraintName="fk_commande_utilisateur"
            onDelete="CASCADE"/>
    
    <!-- Index -->
    <createIndex indexName="idx_commande_numero" tableName="commande">
        <column name="numero_commande"/>
    </createIndex>
    
    <createIndex indexName="idx_commande_statut" tableName="commande">
        <column name="statut"/>
    </createIndex>
    
    <!-- Valeur par défaut -->
    <addDefaultValue tableName="commande" 
                     columnName="statut" 
                     defaultValue="EN_ATTENTE"/>
    
    <!-- Rollback -->
    <rollback>
        <dropTable tableName="commande"/>
    </rollback>
</changeSet>
```

#### Exemple 2 : Ajouter une colonne optionnelle

```xml
<changeSet id="1.2.0-add-column-utilisateur-telephone" author="jane.smith">
    <comment>Ajout du numéro de téléphone pour les notifications SMS</comment>
    
    <addColumn tableName="utilisateur">
        <column name="telephone" type="VARCHAR(20)">
            <constraints nullable="true"/>
        </column>
    </addColumn>
    
    <!-- Index pour recherche -->
    <createIndex indexName="idx_utilisateur_telephone" tableName="utilisateur">
        <column name="telephone"/>
    </createIndex>
    
    <rollback>
        <dropColumn tableName="utilisateur" columnName="telephone"/>
    </rollback>
</changeSet>
```

#### Exemple 3 : Migration de données

```xml
<changeSet id="1.3.0-migrate-statut-deals" author="admin">
    <comment>Migration des anciens statuts vers les nouveaux</comment>
    
    <!-- Mettre à jour les statuts -->
    <update tableName="deal">
        <column name="statut" value="ACTIF"/>
        <where>statut = 'OUVERT'</where>
    </update>
    
    <update tableName="deal">
        <column name="statut" value="TERMINE"/>
        <where>statut = 'FERME'</where>
    </update>
    
    <!-- Pas de rollback pour les migrations de données -->
</changeSet>
```

---

**Date de création** : 19 février 2026  
**Version** : 1.0  
**Auteur** : Équipe PayToGether

