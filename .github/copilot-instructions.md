# Instructions Copilot - Projet PayToGether

## 🌍 Configuration des domaines

- **Frontend** : `dev.dealtogether.ca`
- **Backend (BFF)** : `devbff.dealtogether.ca`
- **Registry privé** : `registry.dealtogether.ca`

### Images Docker
- BFF : `registry.entreprise.com/bffpaytogether:latest`
- Frontend : `registry.entreprise.com/frontpaytogether:latest`

---

## 📐 Architecture Hexagonale & DDD

Le projet PayToGether utilise une **architecture hexagonale** (ports & adapters) combinée avec le **Domain-Driven Design (DDD)**.

### Modules du projet

#### 1. **BFF-CORE** (Cœur métier)
- **Rôle** : Couche métier indépendante de toute technologie
- **Contient** :
  - `modele/` : Modèles métier (suffixe `Modele`, ex: `DealModele`, `UtilisateurModele`)
  - `domaine/service/` : Interfaces de service (ex: `DealService`, `UtilisateurService`)
  - `domaine/impl/` : Implémentations des services (suffixe `ServiceImpl`)
  - `domaine/validator/` : Validateurs métier (ex: `DealValidator`)
  - `provider/` : Interfaces des ports (ex: `DealProvider`, `UtilisateurProvider`)
  - `enumeration/` : Énumérations métier (ex: `StatutDeal`, `StatutImage`)

**Règles** :
- ✅ Aucune dépendance technique (pas de JPA, Spring Web, etc.)
- ✅ Modèles avec suffixe `Modele`
- ✅ Services utilisent uniquement les interfaces Provider
- ✅ Validation métier avant appel au Provider

#### 2. **BFF-PROVIDER** (Adaptateurs techniques - Partie droite)
- **Rôle** : Implémentation de la persistance et des services externes
- **Contient** :
  - `adapter/` : Adaptateurs JPA (suffixe `ProviderAdapter`)
  - `adapter/entity/` : Entités JPA (suffixe `Jpa`, ex: `DealJpa`, `UtilisateurJpa`)
  - `adapter/mapper/` : Mappers JPA ↔ Modèle (suffixe `JpaMapper`)
  - `repository/` : Repositories Spring Data JPA (suffixe `Repository`)
  - `utils/` : Utilitaires techniques (ex: `FileManager`, `Tools`)

**Règles** :
- ✅ Entités JPA avec suffixe `Jpa`
- ✅ Mappers avec méthodes : `versModele()`, `versEntite()`, `mettreAJour()`
- ✅ ProviderAdapter implémente l'interface Provider du core
- ✅ Gestion des transactions (@Transactional)

#### 3. **BFF-API** (Adaptateurs API - Partie gauche)
- **Rôle** : Exposition des API REST
- **Contient** :
  - `resource/` : Contrôleurs REST (suffixe `Resource`)
  - `dto/` : Data Transfer Objects (suffixe `DTO`)
  - `apiadapter/` : Adaptateurs API (suffixe `ApiAdapter`)
  - `mapper/` : Mappers DTO ↔ Modèle (suffixe `Mapper`)

**Règles** :
- ✅ DTOs avec validation Jakarta (`@NotNull`, `@NotBlank`, `@Size`, etc.)
- ✅ Resources exposent les endpoints REST
- ✅ ApiAdapter utilise uniquement les Services du core
- ✅ Mappers avec méthodes : `modeleVersDto()`, `dtoVersModele()`

#### 4. **BFF-FRONT** (Interface utilisateur)
- **Rôle** : Application React/TypeScript
- **Stack** : React, TypeScript, Vite

#### 5. **BFF-WSCLIENT** (Clients externes)
- **Rôle** : Communication avec services externes (JWT, authentification)
- **Stack** : WebClient (Spring WebFlux)

#### 6. **BFF-CONFIGURATION**
- **Rôle** : Configuration centralisée Spring Boot

---

## 🎨 Conventions de nommage

### Variables et méthodes
- ✅ **Français uniquement**
- ✅ **camelCase** (ex: `prixDeal`, `dateCreation`, `listeImages`)

### Classes et Interfaces
- Modèle Core : `{Entité}Modele` (ex: `DealModele`)
- Entité JPA : `{Entité}Jpa` (ex: `DealJpa`)
- DTO : `{Entité}DTO` (ex: `DealDTO`)
- Service : `{Entité}Service` (interface) + `{Entité}ServiceImpl` (implémentation)
- Provider : `{Entité}Provider` (interface) + `{Entité}ProviderAdapter` (implémentation)
- Repository : `{Entité}Repository` (ex: `DealRepository`)
- Mapper JPA : `{Entité}JpaMapper` (ex: `DealJpaMapper`)
- Mapper API : `{Entité}Mapper` (ex: `DealMapper`)
- Resource : `{Entité}Resource` (ex: `DealResource`)
- ApiAdapter : `{Entité}ApiAdapter` (ex: `DealApiAdapter`)

### Méthodes courantes
**Provider/Repository** :
- `sauvegarder()`, `trouverParUuid()`, `trouverTous()`, `mettreAJour()`, `supprimerParUuid()`

**Service** :
- `creer()`, `lireParUuid()`, `lireTous()`, `mettreAJour()`, `supprimerParUuid()`

**ApiAdapter** :
- `creer()`, `trouverParUuid()`, `trouverTous()`, `mettreAJour()`, `supprimer()`

---

## 🖼️ Gestion des images avec MinIO

### Principe
- Upload direct depuis le frontend vers MinIO via **URL présignées**
- Backend génère les URL présignées et gère les statuts

### Statuts des images
```java
public enum StatutImage {
    PENDING,   // En attente d'upload
    UPLOADED,  // Uploadé avec succès
    FAILED     // Échec
}
```

### Flux d'upload
1. **Création** : Frontend crée l'entité avec métadonnées images (statut `PENDING`)
2. **Génération URL** : Backend génère automatiquement `presignUrl` (méthode `PUT`)
3. **Upload** : Frontend upload directement vers MinIO avec `presignUrl`
4. **Confirmation** : Frontend appelle `PATCH /{entityUuid}/images/{imageUuid}/confirm`
5. **Lecture** : Frontend récupère URL de lecture via `GET /{entityUuid}/images/{imageUuid}/url`

### FileManager (bff-provider/utils)
```java
// Génération URL présignée pour upload (PUT)
String generatePresignedUrl(String fullFileName)

// Génération URL présignée pour lecture (GET)
String generatePresignedUrlForRead(String fullFileName)
```

### Pattern dans ProviderAdapter
```java
@Override
public DealModele sauvegarder(DealModele deal) {
    // 1. Mapper vers JPA
    DealJpa entite = mapper.versEntite(deal);
    
    // 2. Ajouter timestamp unique aux noms d'images
    if (deal.getListeImages() != null) {
        deal.getListeImages().forEach(image -> {
            image.setUrlImage(image.getUrlImage() + "_" + System.currentTimeMillis());
            image.setStatut(StatutImage.PENDING);
        });
    }
    
    // 3. Sauvegarder
    DealJpa sauvegarde = jpaRepository.save(entite);
    DealModele modele = mapper.versModele(sauvegarde);
    
    // 4. Générer URL présignées pour images PENDING
    setPresignUrl(modele);
    
    return modele;
}

private void setPresignUrl(DealModele modele) {
    if (modele.getListeImages() != null) {
        modele.getListeImages().stream()
            .filter(img -> img.getStatut() == StatutImage.PENDING)
            .forEach(img -> {
                String presignUrl = fileManager.generatePresignedUrl(img.getUrlImage());
                img.setPresignUrl(presignUrl);
            });
    }
}
```

### Endpoints requis pour chaque entité avec images
```java
// Confirmation upload
@PatchMapping("/{entityUuid}/images/{imageUuid}/confirm")
ResponseEntity<Void> confirmerUploadImage(@PathVariable UUID entityUuid, @PathVariable UUID imageUuid)

// URL de lecture
@GetMapping("/{entityUuid}/images/{imageUuid}/url")
ResponseEntity<Map<String, String>> obtenirUrlImage(@PathVariable UUID entityUuid, @PathVariable UUID imageUuid)
```

### Méthodes à implémenter dans Provider
```java
void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut);
String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid);
```

---

## 🔄 Flux de données (Architecture Hexagonale)

```
Frontend (React)
    ↓ HTTP Request
Resource (bff-api)
    ↓ DTO
ApiAdapter (bff-api)
    ↓ Mapper: DTO → Modele
Service (bff-core)
    ↓ Validation métier
Provider (interface bff-core)
    ↓ Implémentation
ProviderAdapter (bff-provider)
    ↓ Mapper: Modele → Jpa
Repository (bff-provider)
    ↓ JPA
Base de données (PostgreSQL)
```

---

## 📝 Checklist pour créer un nouveau CRUD

### 1. BFF-CORE
- [ ] Créer `{Entité}Modele` dans `modele/`
- [ ] Créer énumérations si nécessaire dans `enumeration/`
- [ ] Créer interface `{Entité}Provider` dans `provider/`
- [ ] Créer interface `{Entité}Service` dans `domaine/service/`
- [ ] Créer `{Entité}ServiceImpl` dans `domaine/impl/`
- [ ] Créer `{Entité}Validator` dans `domaine/validator/` si validation complexe

### 2. BFF-PROVIDER
- [ ] Créer `{Entité}Jpa` dans `adapter/entity/`
- [ ] Créer `{Entité}Repository` dans `repository/`
- [ ] Créer `{Entité}JpaMapper` dans `adapter/mapper/`
- [ ] Créer `{Entité}ProviderAdapter` dans `adapter/`
- [ ] Si images : implémenter méthodes de gestion des images

### 3. BFF-API
- [ ] Créer `{Entité}DTO` dans `dto/`
- [ ] Créer `{Entité}Mapper` dans `mapper/`
- [ ] Créer `{Entité}ApiAdapter` dans `apiadapter/`
- [ ] Créer `{Entité}Resource` dans `resource/`
- [ ] Si images : ajouter endpoints de confirmation et lecture

### 4. Tests
- [ ] Créer `{Entité}ServiceImplTest` (bff-core/test)
- [ ] Créer `{Entité}ProviderAdapterTest` (bff-provider/test)
- [ ] Créer `{Entité}ApiAdapterTest` (bff-api/test)
- [ ] Créer `{Entité}ResourceTest` (bff-api/test)

### 5. HTTP
- [ ] Créer `{entité}.http` dans `bff-http/`

---

## 🧪 Tests

### Pattern de test
```java
@ExtendWith(MockitoExtension.class)
class {Entité}ServiceImplTest {
    @Mock
    private {Entité}Provider provider;
    
    @InjectMocks
    private {Entité}ServiceImpl service;
    
    @Test
    void testCreer_DevraitCreer{Entité}() {
        // Given
        // When
        // Then
    }
}
```

### Nombre de tests recommandé
- ServiceImpl : 10+ tests (CRUD + cas d'erreur)
- ProviderAdapter : 10+ tests (CRUD + cas d'erreur)
- ApiAdapter : 10+ tests
- Resource : 14+ tests (tous les endpoints + validation)

---

## 📅 Format des dates

### JSON
```json
{
  "dateDebut": "2026-02-09T10:00:00",
  "dateFin": "2026-12-31T23:59:59"
}
```

### Annotation DTO
```java
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime dateCreation;
```

---

## 📚 Documentation

### Avant de créer de la documentation
**TOUJOURS demander confirmation avant de générer de la documentation.**

### Structure documentation
- `.github/documentation/` : Documentation technique
- `.github/instructions/` : Instructions de développement
- `modules/bff/bff-http/` : Fichiers de test HTTP

---

## 🔧 Configuration Maven

### POM Parent
Gère les versions et dépendances communes.

### POM Modules
Chaque module a son `pom.xml` avec dépendances spécifiques.

---

## 🎯 Règles d'or

1. ✅ **Toujours** respecter l'architecture hexagonale
2. ✅ **Jamais** de dépendance technique dans bff-core
3. ✅ **Toujours** valider dans le Service avant d'appeler le Provider
4. ✅ **Toujours** utiliser des suffixes explicites (`Modele`, `Jpa`, `DTO`)
5. ✅ **Toujours** mapper entre les couches (ne pas exposer les entités JPA)
6. ✅ **Toujours** générer les URL présignées pour images avec statut PENDING
7. ✅ **Toujours** ajouter timestamp unique aux noms de fichiers
8. ✅ **Toujours** utiliser FileManager pour MinIO
9. ✅ **Toujours** créer les tests unitaires
10. ✅ **Toujours** documenter les endpoints dans fichiers .http

---

**Date de dernière mise à jour** : 9 février 2026  
**Auteur** : Équipe PayToGether
