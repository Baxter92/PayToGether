# PayToGether - Documentation du Modèle de Données et Architecture

## Vue d'ensemble

Ce document décrit l'architecture et le modèle de données créé pour l'application PayToGether, suivant les principes de l'architecture hexagonale (Clean Architecture) et du Domain-Driven Design (DDD).

## Architecture des Modules

### 1. **bff-core** - Domaine Métier
Module contenant le cœur de la logique métier :
- **Entités** : Définition des objets métier avec annotations JPA
- **Énumérations** : Types énumérés pour les statuts, rôles, etc.
- **Localisation** : `com.ulr.paytogether.core.domaine`

#### Entités Créées
1. **Utilisateur** - Gestion des utilisateurs (nom, prénom, email, rôle, statut)
2. **Deal** - Offres groupées avec participants et points forts
3. **Categorie** - Catégorisation des deals
4. **Commentaire** - Système de commentaires hiérarchiques avec likes
5. **Paiement** - Transactions financières avec détails de facturation
6. **Commande** - Gestion des commandes vendeur
7. **Adresse** - Adresses des utilisateurs
8. **Notification** - Système de notifications utilisateur
9. **PointFort** - Points forts des deals
10. **ImageDeal** - Images associées aux deals
11. **SessionUtilisateur** - Gestion des sessions avec JWT
12. **Role** - Définition des rôles système
13. **Commission** - Configuration des commissions
14. **Payout** - Versements aux vendeurs
15. **Publicite** - Gestion des publicités
16. **ImagePub** - Images des publicités

#### Énumérations
- `StatutUtilisateur` : ACTIF, INACTIF
- `RoleUtilisateur` : ADMIN, UTILISATEUR, VENDEUR
- `StatutDeal` : BROUILLON, PUBLIE
- `StatutPaiement` : EN_ATTENTE, CONFIRME, ECHOUE
- `MethodePaiement` : CARTE_CREDIT, INTERAC, VIREMENT_BANCAIRE
- `TypePaiement` : PARTICIPATION, FRAIS_SERVICE
- `Devise` : CAD, USD
- `StatutCommande` : EN_COURS, CONFIRMEE, ANNULEE, REMBOURSEE
- `TypeNotification` : INFO, AVERTISSEMENT, ERREUR
- `StatutPayout` : EN_ATTENTE, TRAITE, ECHOUE
- `MethodePayout` : VIREMENT_BANCAIRE, CHEQUE
- `StatutPublicite` : ACTIVE, INACTIVE
- `EmplacementPublicite` : BANNIERE_HAUT, BANNIERE_BAS, SIDEBAR

### 2. **bff-provider** - Couche d'Infrastructure
Module gérant les aspects techniques :
- **Repositories** : Accès aux données avec Spring Data JPA
- **Configuration WebClient** : Clients HTTP pour services externes
- **Localisation** : `com.ulr.paytogether.provider`

#### Repositories Créés
Chaque entité possède son repository avec des méthodes de recherche personnalisées :
- `UtilisateurRepository` - Recherche par email
- `DealRepository` - Recherche par statut, créateur, catégorie
- `CategorieRepository` - Recherche par nom
- `CommentaireRepository` - Recherche par deal, auteur
- `PaiementRepository` - Recherche par utilisateur, deal, statut
- `CommandeRepository` - Recherche par numéro, utilisateur, deal
- `AdresseRepository` - Recherche par utilisateur, adresse principale
- `NotificationRepository` - Recherche par utilisateur, notifications non lues
- Et tous les autres repositories pour les entités restantes...

#### Configuration WebClient
Classe `WebClientConfiguration` avec beans pour :
- API d'authentification
- API de paiement
- Configuration des URL via properties

### 3. **bff-api** - Couche de Services
Module exposant la logique métier via des services :
- **Services CRUD** : Opérations Create, Read, Update, Delete
- **Localisation** : `com.ulr.paytogether.api.service`

#### Services Créés
Tous les services suivent le même pattern avec :
- Méthodes CRUD complètes (creer, lireParUuid, lireTous, mettreAJour, supprimer)
- Méthodes de recherche spécifiques
- Gestion transactionnelle avec `@Transactional`
- Logging avec Slf4j

Services principaux :
- `UtilisateurService`
- `DealService`
- `CategorieService`
- `CommentaireService`
- `PaiementService`
- `CommandeService`
- `AdresseService`
- `NotificationService`
- `SessionUtilisateurService`
- `PayoutService`
- `PubliciteService`

#### Tests Unitaires
Exemple fourni : `UtilisateurServiceTest`
- Utilisation de JUnit 5 et Mockito
- Couverture des scénarios CRUD
- Tests des cas d'erreur

### 4. **bff-wsclient** - Client WebService
Module dédié à la gestion JWT :
- **JwtService** : Génération et validation des tokens JWT
  - Génération de tokens avec claims personnalisées
  - Extraction des informations (UUID, email, rôle)
  - Validation et vérification d'expiration
  - Configuration via properties (secret, expiration)

### 5. **bff-configuration** - Configuration Globale
- Configuration Spring Boot centralisée
- Profils d'environnement (dev, hml, prod)
- Configuration base de données PostgreSQL
- Paramètres JWT et API externes

## Configuration

### Fichiers de Configuration
La configuration utilise le format **properties** au lieu de YAML :
- `application.properties` - Configuration principale
- `application-dev.properties` - Profil développement
- `application-hml.properties` - Profil homologation
- `application-prod.properties` - Profil production

### Base de Données
```properties
# Configuration principale
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update

# Développement (application-dev.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/paytogether_dev
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}

# Les entités sont automatiquement créées avec hibernate.ddl-auto=update
```

### JWT
```properties
jwt.secret=${JWT_SECRET:monSecretSuperSecurisePourPayTogether2024!}
jwt.expiration=${JWT_EXPIRATION:86400000}
# Expiration par défaut : 24 heures (86400000 ms)
```

### API Externes
```properties
api.auth.url=${API_AUTH_URL:http://localhost:8081}
api.auth.apiKey=${API_AUTH_KEY:votre-cle-api-auth}
api.paiement.url=${API_PAIEMENT_URL:http://localhost:8082}
api.paiement.apiKey=${API_PAIEMENT_KEY:votre-cle-api-paiement}
```

## Convention de Nommage

- **Variables** : camelCase en français (ex: `dateCreation`, `numeroCommande`)
- **Classes** : PascalCase (ex: `Utilisateur`, `DealService`)
- **Méthodes** : camelCase en français (ex: `creer`, `lireParUuid`, `mettreAJour`, `supprimer`)
- **Tables** : snake_case en français (ex: `utilisateur`, `deal`, `numero_commande`)

## Relations entre Entités

### Relations Principales
- **Utilisateur** ↔ **Deal** : Un utilisateur crée plusieurs deals, plusieurs utilisateurs participent à un deal
- **Deal** ↔ **Categorie** : Plusieurs deals dans une catégorie
- **Deal** ↔ **Commentaire** : Un deal possède plusieurs commentaires
- **Commentaire** ↔ **Commentaire** : Commentaires hiérarchiques (parent/enfants)
- **Utilisateur** ↔ **Paiement** : Un utilisateur effectue plusieurs paiements
- **Deal** ↔ **Paiement** : Un deal reçoit plusieurs paiements
- **Commande** ↔ **Paiement** : Une commande contient plusieurs paiements
- **Utilisateur** ↔ **Adresse** : Un utilisateur possède plusieurs adresses
- **Utilisateur** ↔ **SessionUtilisateur** : Un utilisateur a plusieurs sessions

## Dépendances Maven

### bff-core
- spring-boot-starter-data-jpa
- lombok
- spring-boot-starter-validation

### bff-provider
- bff-core
- spring-boot-starter-data-jpa
- spring-boot-starter-webflux
- postgresql
- h2 (tests)

### bff-api
- bff-core
- bff-provider
- spring-boot-starter-web
- spring-boot-starter-test
- mockito-core

### bff-wsclient
- spring-boot-starter-webflux
- jjwt (API, impl, jackson)
- lombok
- slf4j

## Utilisation

### Exemple de Création d'un Utilisateur
```java
@Autowired
private UtilisateurService utilisateurService;

Utilisateur utilisateur = Utilisateur.builder()
    .nom("Dupont")
    .prenom("Jean")
    .email("jean.dupont@example.com")
    .motDePasse("hashedPassword")
    .statut(StatutUtilisateur.ACTIF)
    .role(RoleUtilisateur.UTILISATEUR)
    .build();

Utilisateur created = utilisateurService.creer(utilisateur);
```

### Exemple de Génération JWT
```java
@Autowired
private JwtService jwtService;

String token = jwtService.genererToken(
    utilisateur.getUuid(),
    utilisateur.getEmail(),
    utilisateur.getRole().name()
);
```

### Exemple de Recherche de Deals
```java
@Autowired
private DealService dealService;

// Tous les deals publiés
List<Deal> dealsPublies = dealService.lireParStatut(StatutDeal.PUBLIE);

// Deals d'une catégorie
List<Deal> dealsCategorie = dealService.lireParCategorie(categorieUuid);
```

## Prochaines Étapes

1. **Créer les contrôleurs REST** dans bff-api pour exposer les endpoints HTTP
2. **Implémenter la sécurité** avec Spring Security et JWT
3. **Ajouter des DTO** pour la couche présentation
4. **Implémenter les mappers** entre entités et DTO
5. **Créer les services métier complexes** (orchestration de plusieurs entités)
6. **Ajouter la validation des données** avec Bean Validation
7. **Implémenter la pagination** pour les listes
8. **Ajouter les événements métier** avec Spring Events
9. **Configurer les migrations de base de données** avec Flyway ou Liquibase
10. **Compléter les tests** (intégration, E2E)

## Support

Pour toute question ou problème, consultez :
- Documentation Spring Boot : https://spring.io/projects/spring-boot
- Architecture Hexagonale : https://alistair.cockburn.us/hexagonal-architecture/
- DDD : https://martinfowler.com/tags/domain%20driven%20design.html
