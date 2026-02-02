# PayToGether - Guide de Démarrage Rapide

## 🚀 Installation et Lancement

### Prérequis
- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Docker (optionnel pour PostgreSQL)

### 1. Démarrage de PostgreSQL

#### Option A : Avec Docker (Recommandé)
```bash
# Créer un conteneur PostgreSQL
docker run --name paytogether-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=paytogether_dev \
  -p 5432:5432 \
  -d postgres:14

# Vérifier que le conteneur fonctionne
docker ps
```

#### Option B : Installation Locale
```bash
# macOS avec Homebrew
brew install postgresql@14
brew services start postgresql@14

# Créer la base de données
psql postgres
CREATE DATABASE paytogether_dev;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE paytogether_dev TO postgres;
\q
```

### 2. Configuration de l'Application

#### Fichier : `bff-configuration/src/main/resources/application.properties`
```properties
# Déjà configuré, mais vous pouvez modifier :
spring.datasource.url=jdbc:postgresql://localhost:5432/paytogether_dev
spring.datasource.username=postgres
spring.datasource.password=postgres
```

#### Fichier : `bff-configuration/src/main/resources/application-dev.properties`
```properties
# Configuration spécifique au développement
spring.datasource.url=jdbc:postgresql://localhost:5432/paytogether_dev
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.app.backend-url=http://bfb.dev.svc.cluster.local
```

#### Variables d'environnement (Optionnel)
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=monSecretSuperSecurisePourPayTogether2024!
export JWT_EXPIRATION=86400000
```

### 3. Compilation du Projet

```bash
# Se placer dans le répertoire du projet
cd /Users/da/Documents/NewProjet/PayToGether

# Nettoyer et compiler
mvn clean install

# Ou sans les tests pour aller plus vite
mvn clean install -DskipTests
```

### 4. Vérification de l'Installation

```bash
# Exécuter le script de vérification
./verify-installation.sh
```

## 📊 Structure du Projet Créé

```
PayToGether/
├── 📁 bff-core/              → Entités du domaine (16 entités)
├── 📁 bff-provider/          → Repositories (16 repositories)
├── 📁 bff-api/               → Services CRUD (11 services)
├── 📁 bff-wsclient/          → Service JWT
├── 📁 bff-configuration/     → Configuration Spring Boot
├── 📄 MODEL_DOCUMENTATION.md → Documentation complète
├── 📄 IMPLEMENTATION_SUMMARY.md → Résumé de l'implémentation
├── 📄 ENTITY_RELATIONSHIPS.md → Diagramme des relations
└── 📄 QUICK_START.md         → Ce fichier
```

## 🧪 Tests

### Exécuter les Tests Unitaires
```bash
# Tous les tests
mvn test

# Tests d'un module spécifique
mvn test -pl bff-api

# Test d'une classe spécifique
mvn test -Dtest=UtilisateurServiceTest
```

### Exemple de Test Créé
```java
// bff-api/src/test/java/.../UtilisateurServiceTest.java
// Tests complets du service Utilisateur avec Mockito
```

## 🔑 Utilisation du Service JWT

### Générer un Token
```java
@Autowired
private JwtService jwtService;

String token = jwtService.genererToken(
    UUID.randomUUID(),
    "jean.dupont@example.com",
    "UTILISATEUR"
);
```

### Valider un Token
```java
boolean isValid = jwtService.validerToken(token, "jean.dupont@example.com");
UUID userId = jwtService.extraireUuidUtilisateur(token);
String role = jwtService.extraireRole(token);
```

## 💾 Utilisation des Services CRUD

### Exemple : Créer un Utilisateur
```java
@Autowired
private UtilisateurService utilisateurService;

Utilisateur utilisateur = Utilisateur.builder()
    .nom("Dupont")
    .prenom("Jean")
    .email("jean.dupont@example.com")
    .motDePasse("hashedPassword") // À hasher avec BCrypt
    .statut(StatutUtilisateur.ACTIF)
    .role(RoleUtilisateur.UTILISATEUR)
    .build();

Utilisateur created = utilisateurService.creer(utilisateur);
```

### Exemple : Créer un Deal
```java
@Autowired
private DealService dealService;
@Autowired
private UtilisateurService utilisateurService;
@Autowired
private CategorieService categorieService;

Utilisateur createur = utilisateurService.lireParUuid(createurUuid).orElseThrow();
Categorie categorie = categorieService.lireParUuid(categorieUuid).orElseThrow();

Deal deal = Deal.builder()
    .titre("Lot de 10 kg de bœuf premium")
    .description("Viande de qualité AAA")
    .prixDeal(new BigDecimal("200.00"))
    .prixPart(new BigDecimal("20.00"))
    .nbParticipants(10)
    .dateDebut(LocalDateTime.now())
    .dateFin(LocalDateTime.now().plusDays(7))
    .statut(StatutDeal.PUBLIE)
    .createur(createur)
    .categorie(categorie)
    .ville("Montréal")
    .pays("Canada")
    .build();

Deal created = dealService.creer(deal);
```

### Exemple : Recherche
```java
// Tous les deals publiés
List<Deal> dealsPublies = dealService.lireParStatut(StatutDeal.PUBLIE);

// Deals d'un créateur
List<Deal> mesDeals = dealService.lireParCreateur(utilisateurUuid);

// Utilisateur par email
Optional<Utilisateur> user = utilisateurService.lireParEmail("jean@example.com");

// Notifications non lues
List<Notification> nonLues = notificationService.lireNonLues(utilisateurUuid);
```

## 🌐 Appels API Externes (WebClient)

### Configuration
```java
// Déjà configuré dans WebClientConfiguration
@Autowired
@Qualifier("webClientAuth")
private WebClient webClientAuth;

@Autowired
@Qualifier("webClientPaiement")
private WebClient webClientPaiement;
```

### Exemple d'Utilisation
```java
// Appel asynchrone à l'API d'authentification
Mono<AuthResponse> response = webClientAuth
    .post()
    .uri("/authenticate")
    .bodyValue(authRequest)
    .retrieve()
    .bodyToMono(AuthResponse.class);
```

## 📚 Documentation Disponible

### Fichiers Créés
1. **MODEL_DOCUMENTATION.md** - Documentation technique complète
   - Architecture détaillée
   - Description de toutes les entités
   - Exemples d'utilisation
   - Configuration

2. **IMPLEMENTATION_SUMMARY.md** - Résumé de l'implémentation
   - Liste complète de ce qui a été créé
   - Technologies utilisées
   - Prochaines étapes

3. **ENTITY_RELATIONSHIPS.md** - Relations entre entités
   - Diagrammes ASCII
   - Cardinalités
   - Agrégats DDD

4. **QUICK_START.md** - Guide de démarrage (ce fichier)

## 🐛 Dépannage

### Problème : Maven non trouvé
```bash
# Vérifier l'installation de Maven
mvn --version

# Si non installé (macOS)
brew install maven
```

### Problème : PostgreSQL ne démarre pas
```bash
# Vérifier le statut
docker ps -a  # Si Docker
brew services list  # Si installation locale

# Logs Docker
docker logs paytogether-postgres

# Redémarrer
docker restart paytogether-postgres
```

### Problème : Erreur de connexion à la base de données
```bash
# Vérifier que PostgreSQL écoute sur le bon port
netstat -an | grep 5432

# Tester la connexion
psql -h localhost -U postgres -d paytogether_dev
```

### Problème : Tables non créées
```properties
# Dans application.properties, vérifier :
spring.jpa.hibernate.ddl-auto=update
# Doit être 'update' ou 'create'
```

## 📝 Prochaines Étapes Recommandées

### 1. Créer les Contrôleurs REST
```java
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {
    @Autowired
    private UtilisateurService utilisateurService;
    
    @PostMapping
    public ResponseEntity<Utilisateur> creer(@Valid @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(utilisateurService.creer(utilisateur));
    }
}
```

### 2. Implémenter Spring Security
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 3. Ajouter les DTO et Mappers
```java
// Éviter d'exposer directement les entités
public class UtilisateurDTO {
    private UUID uuid;
    private String nom;
    private String prenom;
    private String email;
    // Pas de motDePasse exposé
}
```

### 4. Ajouter Swagger/OpenAPI
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 5. Configurer les Migrations de Base de Données
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## 🎯 Commandes Utiles

```bash
# Nettoyer le projet
mvn clean

# Compiler
mvn compile

# Empaqueter
mvn package

# Installer dans le repo local
mvn install

# Vérifier les dépendances
mvn dependency:tree

# Mise à jour des dépendances
mvn versions:display-dependency-updates

# Exécuter l'application (après avoir ajouté @SpringBootApplication)
mvn spring-boot:run

# Générer le Javadoc
mvn javadoc:javadoc

# Vérifier le code avec Checkstyle (si configuré)
mvn checkstyle:check

# Rapport de couverture de tests (avec JaCoCo)
mvn jacoco:report
```

## 📞 Support et Ressources

- Documentation Spring Boot : https://spring.io/projects/spring-boot
- Spring Data JPA : https://spring.io/projects/spring-data-jpa
- PostgreSQL : https://www.postgresql.org/docs/
- JWT : https://jwt.io/
- Lombok : https://projectlombok.org/

## ✅ Checklist de Vérification

- [ ] PostgreSQL installé et démarré
- [ ] Base de données `paytogether_dev` créée
- [ ] Java 21 installé
- [ ] Maven installé et configuré
- [ ] Projet compilé sans erreur (`mvn clean install`)
- [ ] Tests passent (`mvn test`)
- [ ] Documentation lue (`MODEL_DOCUMENTATION.md`)
- [ ] Relations comprises (`ENTITY_RELATIONSHIPS.md`)

---

**Votre architecture hexagonale avec DDD est prête à être utilisée ! 🎉**
