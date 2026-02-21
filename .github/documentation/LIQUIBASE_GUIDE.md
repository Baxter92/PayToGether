# Guide d'utilisation de Liquibase - PayToGether

## 📋 Vue d'ensemble

Liquibase est intégré dans le projet PayToGether pour gérer les versions du schéma de base de données. Il remplace le mode `spring.jpa.hibernate.ddl-auto=update` pour offrir un contrôle total sur les migrations de base de données.

## 🏗️ Structure des fichiers

```
bff-configuration/src/main/resources/db/changelog/
├── db.changelog-master.xml              # Fichier principal qui inclut tous les changelogs
└── versions/
    ├── v1.0.0-schema-initial.xml        # Schéma initial (tables, index, contraintes)
    ├── v1.0.0-donnees-initiales.xml     # Données de référence (catégories)
    └── v1.x.x-nom-changement.xml        # Futures migrations
```

## ⚙️ Configuration

### application.properties

```properties
# Désactiver Hibernate DDL Auto
spring.jpa.hibernate.ddl-auto=none

# Configuration Liquibase
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.default-schema=public
spring.liquibase.liquibase-schema=public
```

### Dépendances Maven (bff-configuration/pom.xml)

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

## 🚀 Utilisation

### Au démarrage de l'application

Liquibase s'exécute automatiquement au démarrage de Spring Boot et :
1. Vérifie si les tables de tracking Liquibase existent (`databasechangelog`, `databasechangeloglock`)
2. Exécute tous les changesets qui n'ont pas encore été appliqués
3. Enregistre chaque changeset dans la table `databasechangelog`

### Créer une nouvelle migration

#### 1. Créer un nouveau fichier de changelog

Créez un fichier dans `db/changelog/versions/` avec le format :
```
v{version}-{description-courte}.xml
```

Exemple : `v1.1.0-ajout-table-commande.xml`

#### 2. Structure d'un changelog

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <!-- Description du changement -->
    <changeSet id="1.1.0-create-table-commande" author="votre-nom">
        <createTable tableName="commande">
            <column name="uuid" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="numero_commande" type="VARCHAR(50)">
                <constraints nullable="false" unique="true"/>
            </column>
            <!-- Autres colonnes -->
        </createTable>
        
        <!-- Rollback optionnel -->
        <rollback>
            <dropTable tableName="commande"/>
        </rollback>
    </changeSet>
    
    <!-- Tag de version -->
    <changeSet id="1.1.0-tag-release" author="votre-nom">
        <tagDatabase tag="v1.1.0"/>
    </changeSet>

</databaseChangeLog>
```

#### 3. Inclure dans le master

Ajoutez la ligne dans `db.changelog-master.xml` :

```xml
<include file="db/changelog/versions/v1.1.0-ajout-table-commande.xml"/>
```

## 📝 Bonnes pratiques

### Règles d'or

1. ✅ **Ne jamais modifier un changeset déjà appliqué** en production
2. ✅ **Toujours créer un nouveau changeset** pour chaque modification
3. ✅ **Utiliser des IDs uniques** pour chaque changeset (format : `{version}-{action}-{objet}`)
4. ✅ **Documenter chaque changeset** avec des commentaires clairs
5. ✅ **Tester les migrations** sur un environnement de développement avant production
6. ✅ **Prévoir des rollbacks** pour les changements critiques

### Nommage des changesets

Format recommandé pour l'ID :
```
{version}-{action}-{objet}
```

Exemples :
- `1.0.0-create-table-utilisateur`
- `1.1.0-add-column-utilisateur-telephone`
- `1.2.0-create-index-deal-statut`
- `2.0.0-alter-table-deal-prix`

### Types de changements courants

#### Créer une table
```xml
<changeSet id="x.x.x-create-table-xxx" author="nom">
    <createTable tableName="xxx">
        <column name="uuid" type="UUID">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <!-- colonnes -->
    </createTable>
</changeSet>
```

#### Ajouter une colonne
```xml
<changeSet id="x.x.x-add-column-table-colonne" author="nom">
    <addColumn tableName="table">
        <column name="nouvelle_colonne" type="VARCHAR(100)"/>
    </addColumn>
</changeSet>
```

#### Modifier une colonne
```xml
<changeSet id="x.x.x-modify-column-table-colonne" author="nom">
    <modifyDataType tableName="table" 
                    columnName="colonne" 
                    newDataType="VARCHAR(200)"/>
</changeSet>
```

#### Créer un index
```xml
<changeSet id="x.x.x-create-index-table-colonne" author="nom">
    <createIndex indexName="idx_table_colonne" tableName="table">
        <column name="colonne"/>
    </createIndex>
</changeSet>
```

#### Ajouter une contrainte de clé étrangère
```xml
<changeSet id="x.x.x-add-fk-table1-table2" author="nom">
    <addForeignKeyConstraint
            baseTableName="table1"
            baseColumnNames="table2_uuid"
            referencedTableName="table2"
            referencedColumnNames="uuid"
            constraintName="fk_table1_table2"
            onDelete="CASCADE"/>
</changeSet>
```

#### Insérer des données
```xml
<changeSet id="x.x.x-insert-data-table" author="nom">
    <insert tableName="table">
        <column name="uuid" valueComputed="gen_random_uuid()"/>
        <column name="nom" value="Valeur"/>
        <column name="date_creation" valueDate="CURRENT_TIMESTAMP"/>
    </insert>
</changeSet>
```

## 🔍 Commandes utiles

### Vérifier le statut des migrations

```bash
mvn liquibase:status -pl modules/bff/bff-configuration
```

### Générer un changelog à partir d'une base existante

```bash
mvn liquibase:generateChangeLog -pl modules/bff/bff-configuration
```

### Rollback d'une migration (développement uniquement)

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1 -pl modules/bff/bff-configuration
```

### Marquer toutes les migrations comme exécutées (sans les exécuter)

Utile si vous migrez depuis `ddl-auto=update` vers Liquibase :

```bash
mvn liquibase:changelogSync -pl modules/bff/bff-configuration
```

## 🗄️ Tables Liquibase

Liquibase crée deux tables de tracking :

### databasechangelog
Enregistre tous les changesets appliqués :
- `id` : ID du changeset
- `author` : Auteur du changeset
- `filename` : Fichier source
- `dateexecuted` : Date d'exécution
- `orderexecuted` : Ordre d'exécution
- `exectype` : Type d'exécution (EXECUTED, RERAN, etc.)
- `md5sum` : Checksum du changeset
- `description` : Description du changement
- `tag` : Tag de version (si applicable)

### databasechangeloglock
Gère le verrouillage pour éviter les exécutions concurrentes :
- `id` : ID du lock
- `locked` : État du verrou
- `lockgranted` : Date d'acquisition
- `lockedby` : Qui détient le verrou

## 🔄 Migration depuis Hibernate DDL Auto

Si vous aviez déjà une base de données créée avec `spring.jpa.hibernate.ddl-auto=update` :

### Option 1 : Base de données vide (développement)

1. Supprimez la base de données
2. Redémarrez l'application
3. Liquibase créera tout le schéma

### Option 2 : Base existante (production)

1. Utilisez `changelogSync` pour marquer les migrations initiales comme appliquées :
```bash
mvn liquibase:changelogSync -pl modules/bff/bff-configuration
```

2. Vérifiez que tout est synchronisé :
```bash
mvn liquibase:status -pl modules/bff/bff-configuration
```

## 📚 Références

- [Documentation officielle Liquibase](https://docs.liquibase.com)
- [Spring Boot + Liquibase](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [Liquibase XML Format](https://docs.liquibase.com/concepts/changelogs/xml-format.html)

## 🎯 Checklist pour une nouvelle migration

- [ ] Créer le fichier XML dans `db/changelog/versions/`
- [ ] Utiliser un ID unique et descriptif
- [ ] Documenter le changeset avec des commentaires
- [ ] Inclure le fichier dans `db.changelog-master.xml`
- [ ] Tester sur environnement de développement
- [ ] Prévoir un rollback si nécessaire
- [ ] Vérifier avec `mvn liquibase:status`
- [ ] Commiter le fichier avec les changements d'entités JPA associés
- [ ] Tagger la version si c'est une release majeure

---

**Date de dernière mise à jour** : 19 février 2026  
**Auteur** : Équipe PayToGether

