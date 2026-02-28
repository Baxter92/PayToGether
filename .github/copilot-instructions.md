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
  - `domaine/validator/` : **Validateurs métier avec toutes les règles** (ex: `DealValidator`)
  - `provider/` : Interfaces des ports (ex: `DealProvider`, `UtilisateurProvider`)
  - `enumeration/` : Énumérations métier (ex: `StatutDeal`, `StatutImage`)
  - `exception/` : **Exceptions métier personnalisées avec codes traduisibles**

**Règles** :
- ✅ Aucune dépendance technique (pas de JPA, Spring Web, etc.)
- ✅ Modèles avec suffixe `Modele`
- ✅ Services utilisent uniquement les interfaces Provider
- ✅ **Toutes les règles métier sont dans les Validators**
- ✅ **Toutes les exceptions utilisent des codes d'erreur traduisibles**
- ✅ Validation métier OBLIGATOIRE avant appel au Provider

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

**Validator** :
- `valider()`, `validerPourCreation()`, `validerPourMiseAJour()`

---

## 🔧 Mise à jour séparée : Statut et Images (Pattern PATCH)

### Principe
Pour éviter les conflits et simplifier les mises à jour, certains attributs d'une entité peuvent être mis à jour **séparément** via des endpoints PATCH dédiés. Cela permet de :
- ✅ Séparer les préoccupations (statut, images, informations générales)
- ✅ Éviter les conflits lors de modifications concurrentes
- ✅ Simplifier la validation métier
- ✅ Améliorer la sécurité (contrôle fin des autorisations)

### Pattern général

#### 1. Endpoint PUT général (sans statut et sans images)
```java
@PutMapping("/{uuid}")
public ResponseEntity<DealResponseDto> mettreAJour(
        @PathVariable UUID uuid,
        @Valid @RequestBody MiseAJourDealDTO dto) {
    // MiseAJourDealDTO ne contient PAS statut ni listeImages
    return ResponseEntity.ok(apiAdapter.mettreAJour(uuid, dto));
}
```

#### 2. Endpoint PATCH pour le statut
```java
@PatchMapping("/{uuid}/statut")
public ResponseEntity<DealResponseDto> mettreAJourStatut(
        @PathVariable UUID uuid,
        @Valid @RequestBody MiseAJourStatutDealDTO dto) {
    return ResponseEntity.ok(apiAdapter.mettreAJourStatut(uuid, dto.getStatut()));
}
```

#### 3. Endpoint PATCH pour les images
```java
@PatchMapping("/{uuid}/images")
public ResponseEntity<DealResponseDto> mettreAJourImages(
        @PathVariable UUID uuid,
        @Valid @RequestBody MiseAJourImagesDealDTO dto) {
    // Les anciennes images sont SUPPRIMÉES et remplacées par les nouvelles
    return ResponseEntity.ok(apiAdapter.mettreAJourImages(uuid, dto));
}
```

### DTOs requis

#### MiseAJour{Entité}DTO (sans statut et images)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiseAJourDealDTO {
    @NotBlank
    private String titre;
    private String description;
    @NotNull @Positive
    private BigDecimal prixDeal;
    @NotNull @Positive
    private BigDecimal prixPart;
    @NotNull @Positive
    private Integer nbParticipants;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull
    private LocalDateTime dateFin;
    @NotNull
    private UUID createurUuid;
    @NotNull
    private UUID categorieUuid;
    private List<String> listePointsForts;
    @NotNull
    private String ville;
    private String pays;
    
    // NOTE: Pas de statut ni de listeImages
}
```

#### MiseAJourStatut{Entité}DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiseAJourStatutDealDTO {
    @NotNull(message = "Le statut est obligatoire")
    private StatutDeal statut;
}
```

#### MiseAJourImages{Entité}DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiseAJourImagesDealDTO {
    @NotEmpty(message = "Au moins une image est obligatoire")
    @Valid
    private List<ImageDealDto> listeImages;
}
```

### Méthodes dans le Validator (bff-core)

```java
@Component
public class DealValidator {
    
    /**
     * Validation partielle pour mise à jour sans images et sans statut
     */
    public void validerPourMiseAJourPartielle(DealModele dealModele) {
        if (dealModele == null) {
            throw new ValidationException("deal.null");
        }
        
        if (dealModele.getUuid() == null) {
            throw new ValidationException("deal.uuid.obligatoire");
        }
        
        // Validation de tous les champs SAUF statut et images
        if (dealModele.getTitre() == null || dealModele.getTitre().isBlank()) {
            throw new ValidationException("deal.titre.obligatoire");
        }
        
        if (dealModele.getPrixDeal() == null || dealModele.getPrixDeal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("deal.prixDeal.obligatoire");
        }
        
        if (dealModele.getPrixPart() == null || dealModele.getPrixPart().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("deal.prixPart.obligatoire");
        }
        
        if (dealModele.getNbParticipants() == null || dealModele.getNbParticipants() <= 0) {
            throw new ValidationException("deal.nbParticipants.obligatoire");
        }
        
        if (dealModele.getDateDebut() == null) {
            throw new ValidationException("deal.dateDebut.obligatoire");
        }
        
        if (dealModele.getDateFin() == null) {
            throw new ValidationException("deal.dateFin.obligatoire");
        }
        
        if (dealModele.getCreateurUuid() == null) {
            throw new ValidationException("deal.createurUuid.obligatoire");
        }
        
        if (dealModele.getCategorieUuid() == null) {
            throw new ValidationException("deal.categorieUuid.obligatoire");
        }
        
        if (dealModele.getVille() == null || dealModele.getVille().isBlank()) {
            throw new ValidationException("deal.ville.obligatoire");
        }
        
        if (dealModele.getPays() == null || dealModele.getPays().isBlank()) {
            throw new ValidationException("deal.pays.obligatoire");
        }
    }
    
    /**
     * Validation des transitions de statut
     */
    public void validerTransitionStatut(StatutDeal statutActuel, StatutDeal nouveauStatut) {
        if (nouveauStatut == null) {
            throw new ValidationException("deal.statut.obligatoire");
        }
        
        if (statutActuel == null) {
            throw new ValidationException("deal.statut.actuel.null");
        }
        
        // Règles de transition d'état selon la logique métier
        switch (statutActuel) {
            case BROUILLON:
                if (nouveauStatut != StatutDeal.PUBLIE && nouveauStatut != StatutDeal.BROUILLON) {
                    throw new ValidationException("deal.statut.transition.invalide", statutActuel, nouveauStatut);
                }
                break;
            
            case PUBLIE:
                if (nouveauStatut != StatutDeal.EXPIRE && nouveauStatut != StatutDeal.PUBLIE) {
                    throw new ValidationException("deal.statut.transition.invalide", statutActuel, nouveauStatut);
                }
                break;
            
            case EXPIRE:
                // État final : aucune transition autorisée
                if (nouveauStatut != StatutDeal.EXPIRE) {
                    throw new ValidationException("deal.statut.expire.immuable");
                }
                break;
        }
    }
    
    /**
     * Validation des images
     */
    public void validerImages(DealModele dealModele) {
        if (dealModele == null) {
            throw new ValidationException("deal.null");
        }
        
        if (dealModele.getListeImages() == null || dealModele.getListeImages().isEmpty()) {
            throw new ValidationException("deal.listeImages.obligatoire");
        }
        
        // Une et une seule image principale
        long nombreImagesPrincipales = dealModele.getListeImages().stream()
                .filter(img -> img.getIsPrincipal() != null && img.getIsPrincipal())
                .count();
        
        if (nombreImagesPrincipales == 0) {
            throw new ValidationException("deal.image.principale.manquante");
        }
        
        if (nombreImagesPrincipales > 1) {
            throw new ValidationException("deal.image.principale.unique");
        }
        
        // Chaque image doit avoir une URL
        boolean imagesSansUrl = dealModele.getListeImages().stream()
                .anyMatch(img -> img.getUrlImage() == null || img.getUrlImage().isBlank());
        
        if (imagesSansUrl) {
            throw new ValidationException("deal.image.url.obligatoire");
        }
    }
}
```

### Méthodes dans le Service (bff-core)

```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    
    private final DealProvider dealProvider;
    private final DealValidator dealValidator;
    
    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        // Validation PARTIELLE (sans statut ni images)
        dealValidator.validerPourMiseAJourPartielle(deal);
        return dealProvider.mettreAJour(uuid, deal);
    }
    
    @Override
    public DealModele mettreAJourStatut(UUID uuid, StatutDeal nouveauStatut) {
        // Récupérer le deal existant
        DealModele dealExistant = dealProvider.trouverParUuid(uuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("deal", uuid));
        
        // Valider la transition de statut
        dealValidator.validerTransitionStatut(dealExistant.getStatut(), nouveauStatut);
        
        // Mettre à jour le statut
        return dealProvider.mettreAJourStatut(uuid, nouveauStatut);
    }
    
    @Override
    public DealModele mettreAJourImages(UUID uuid, DealModele deal) {
        // Vérifier que le deal existe
        dealProvider.trouverParUuid(uuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("deal", uuid));
        
        // Valider les images
        dealValidator.validerImages(deal);
        
        // Mettre à jour uniquement les images (suppression + remplacement)
        return dealProvider.mettreAJourImages(uuid, deal);
    }
}
```

### Méthodes dans le Provider (bff-core/provider)

```java
public interface DealProvider {
    // CRUD standard
    DealModele sauvegarder(DealModele deal);
    DealModele mettreAJour(UUID uuid, DealModele deal);
    Optional<DealModele> trouverParUuid(UUID uuid);
    
    // Mise à jour séparée du statut
    DealModele mettreAJourStatut(UUID uuid, StatutDeal statut);
    
    // Mise à jour séparée des images (suppression + remplacement)
    DealModele mettreAJourImages(UUID uuid, DealModele deal);
    
    // Gestion fine des images
    void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut);
    String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid);
}
```

### Implémentation dans ProviderAdapter (bff-provider)

```java
@Component
@RequiredArgsConstructor
public class DealProviderAdapter implements DealProvider {
    
    private final DealRepository jpaRepository;
    private final DealJpaMapper mapper;
    private final FileManager fileManager;
    
    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele mettreAJourStatut(UUID uuid, StatutDeal statut) {
        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé : " + uuid));
        
        deal.setStatut(statut);
        deal.setDateModification(LocalDateTime.now());
        
        DealJpa sauvegarde = jpaRepository.save(deal);
        return mapper.versModele(sauvegarde);
    }
    
    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele mettreAJourImages(UUID uuid, DealModele dealAvecNouvellesImages) {
        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé : " + uuid));
        
        if (dealAvecNouvellesImages.getListeImages() == null || dealAvecNouvellesImages.getListeImages().isEmpty()) {
            return mapper.versModele(deal);
        }

        // Collecter les UUIDs des images envoyées dans le DTO
        List<UUID> uuidsEnvoyes = dealAvecNouvellesImages.getListeImages().stream()
                .map(img -> img.getUuid())
                .filter(uuid1 -> uuid1 != null)
                .toList();

        // 1. SUPPRIMER les images en BD dont l'UUID n'est PAS dans le DTO
        if (deal.getImageDealJpas() != null && !deal.getImageDealJpas().isEmpty()) {
            deal.getImageDealJpas().removeIf(imageJpa -> 
                !uuidsEnvoyes.contains(imageJpa.getUuid())
            );
        }

        // 2. Créer une map des images existantes en BD
        var imagesExistantesParUuid = deal.getImageDealJpas() != null 
            ? deal.getImageDealJpas().stream()
                .collect(Collectors.toMap(
                    ImageDealJpa::getUuid,
                    img -> img,
                    (img1, img2) -> img2
                ))
            : new HashMap<UUID, ImageDealJpa>();

        // 3. Liste des nouvelles images à retourner avec presignUrl
        List<ImageDealModele> nouvellesImagesAvecPresign = new ArrayList<>();

        // 4. Parcourir les images du DTO
        for (var imageModele : dealAvecNouvellesImages.getListeImages()) {
            if (imageModele.getUuid() == null) {
                // 4.1. AJOUTER nouvelle image (UUID null)
                ImageDealJpa nouvelleImage = ImageDealJpa.builder()
                        .uuid(UUID.randomUUID())
                        .urlImage(FilenameUtils.getBaseName(imageModele.getUrlImage())
                                + "_" + System.currentTimeMillis()
                                + "." + FilenameUtils.getExtension(imageModele.getUrlImage()))
                        .isPrincipal(imageModele.getIsPrincipal())
                        .statut(StatutImage.PENDING)
                        .dealJpa(deal)
                        .build();
                
                if (deal.getImageDealJpas() == null) {
                    deal.setImageDealJpas(new ArrayList<>());
                }
                deal.getImageDealJpas().add(nouvelleImage);
                
                // Ajouter à la liste pour générer presignUrl
                ImageDealModele imageModeleAvecUuid = ImageDealModele.builder()
                        .uuid(nouvelleImage.getUuid())
                        .urlImage(nouvelleImage.getUrlImage())
                        .isPrincipal(nouvelleImage.getIsPrincipal())
                        .statut(StatutImage.PENDING)
                        .build();
                nouvellesImagesAvecPresign.add(imageModeleAvecUuid);
                
            } else {
                // 4.2. MODIFIER image existante si isPrincipal change
                ImageDealJpa imageExistante = imagesExistantesParUuid.get(imageModele.getUuid());
                if (imageExistante != null && imageExistante.getIsPrincipal() != imageModele.getIsPrincipal()) {
                    imageExistante.setIsPrincipal(imageModele.getIsPrincipal());
                    imageExistante.setDateModification(LocalDateTime.now());
                }
            }
        }

        deal.setDateModification(LocalDateTime.now());
        DealJpa sauvegarde = jpaRepository.save(deal);
        DealModele modeleSauvegarde = mapper.versModele(sauvegarde);
        
        // 5. Générer les URL présignées UNIQUEMENT pour les nouvelles images
        nouvellesImagesAvecPresign.forEach(img -> {
            String presignUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_DEALS_IMAGES, img.getUrlImage());
            img.setPresignUrl(presignUrl);
        });
        
        // 6. Remplacer la liste par UNIQUEMENT les nouvelles images avec presignUrl
        modeleSauvegarde.setListeImages(nouvellesImagesAvecPresign);
        
        return modeleSauvegarde;
    }
}
```

### Frontend - Utilisation des endpoints PATCH
## 🛡️ Validators et Exceptions Métier

### Principe fondamental
**TOUTES les règles métier DOIVENT être définies dans les Validators (bff-core/domaine/validator/)**

Les Validators sont responsables de :
- ✅ Validation des champs obligatoires
- ✅ Validation des formats (email, code postal, etc.)
- ✅ Validation des longueurs min/max
- ✅ Validation des valeurs (positives, cohérence des dates, etc.)
- ✅ Validation des règles métier complexes (statuts, transitions d'état)

### Structure des Validators

#### Validator de base
```java
@Component
public class {Entité}Validator {
    
    // Constantes de validation
    private static final int MAX_LENGTH = 100;
    
    /**
     * Validation complète pour création
     * @throws ValidationException avec code traduisible
     */
    public void valider({Entité}Modele entite) {
        if (entite == null) {
            throw new ValidationException("entite.null");
        }
        
        // Validations des champs
        if (entite.getNom() == null || entite.getNom().isBlank()) {
            throw new ValidationException("entite.nom.obligatoire");
        }
        
        if (entite.getNom().length() > MAX_LENGTH) {
            throw new ValidationException("entite.nom.longueur", MAX_LENGTH);
        }
    }
    
    /**
     * Validation pour mise à jour (inclut UUID)
     */
    public void validerPourMiseAJour({Entité}Modele entite) {
        if (entite == null) {
            throw new ValidationException("entite.null");
        }
        
        if (entite.getUuid() == null) {
            throw new ValidationException("entite.uuid.obligatoire");
        }
        
        valider(entite);
    }
    
    /**
     * Validations métier spécifiques (transitions d'état, etc.)
     */
    public void validerActivation({Entité}Modele entite) {
        if (entite.getStatut() == Statut.ACTIVE) {
            throw new ValidationException("entite.deja.active");
        }
    }
}
```

### Validators obligatoires par entité

Pour chaque entité métier, créer un Validator dans `bff-core/domaine/validator/` :
- ✅ `DealValidator` : Validation des deals
- ✅ `UtilisateurValidator` : Validation des utilisateurs
- ✅ `CategorieValidator` : Validation des catégories
- ✅ `PubliciteValidator` : Validation des publicités
- ✅ `CommandeValidator` : Validation des commandes
- ✅ `CommentaireValidator` : Validation des commentaires
- ✅ `AdresseValidator` : Validation des adresses
- ✅ `PaiementValidator` : Validation des paiements

### Hiérarchie des Exceptions

Toutes les exceptions métier héritent de `BusinessException` et utilisent des **codes d'erreur traduisibles**.

#### BusinessException (base)
```java
@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final Object[] params;
    
    public BusinessException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }
    
    public BusinessException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }
}
```

#### Exceptions spécialisées

##### ValidationException
```java
public class ValidationException extends BusinessException {
    public ValidationException(String errorCode) {
        super(errorCode);
    }
    
    public ValidationException(String errorCode, Object... params) {
        super(errorCode, params);
    }
}
```

**Utilisation** :
```java
throw new ValidationException("deal.titre.obligatoire");
throw new ValidationException("deal.description.longueur", 5000);
```

##### ResourceNotFoundException
```java
public class ResourceNotFoundException extends BusinessException {
    public static ResourceNotFoundException parUuid(String resourceType, UUID uuid) {
        return new ResourceNotFoundException(resourceType + ".non.trouve", uuid.toString());
    }
}
```

**Utilisation** :
```java
throw ResourceNotFoundException.parUuid("deal", dealUuid);
```

##### DuplicateResourceException
```java
public class DuplicateResourceException extends BusinessException {
    public static DuplicateResourceException emailExistant(String email) {
        return new DuplicateResourceException("utilisateur.email.existe", email);
    }
}
```

**Utilisation** :
```java
throw DuplicateResourceException.emailExistant("test@example.com");
```

##### ForbiddenOperationException
```java
public class ForbiddenOperationException extends BusinessException {
    // Pour les opérations interdites selon les règles métier
}
```

**Utilisation** :
```java
throw new ForbiddenOperationException("commande.deja.annulee");
```

##### FileStorageException
```java
public class FileStorageException extends BusinessException {
    // Pour les erreurs MinIO/stockage de fichiers
}
```

**Utilisation** :
```java
throw new FileStorageException("image.upload.echec", nomFichier);
```

### Format des codes d'erreur

**Pattern** : `{entité}.{attribut}.{type}`

**Exemples** :
- `deal.titre.obligatoire` : Champ obligatoire
- `utilisateur.email.format` : Format invalide
- `commande.deja.annulee` : Règle métier
- `deal.non.trouve` : Ressource non trouvée
- `utilisateur.email.existe` : Duplication

**Avec paramètres** :
- `deal.description.longueur` → "La description ne peut pas dépasser {0} caractères"
- `utilisateur.motDePasse.longueur` → "Le mot de passe doit contenir au moins {0} caractères"

### Utilisation dans les Services

Les Services **DOIVENT TOUJOURS** appeler le Validator avant de faire appel au Provider :

```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    
    private final DealProvider dealProvider;
    private final DealValidator dealValidator;
    
    @Override
    public DealModele creer(DealModele deal) {
        // ✅ OBLIGATOIRE : Validation avant création
        dealValidator.valider(deal);
        
        return dealProvider.sauvegarder(deal);
    }
    
    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        // ✅ OBLIGATOIRE : Validation avant mise à jour
        dealValidator.validerPourMiseAJour(deal);
        
        return dealProvider.mettreAJour(uuid, deal);
    }
}
```

### Gestion des erreurs côté API (bff-api)

Les exceptions sont automatiquement interceptées par un `@ControllerAdvice` et transformées en réponses HTTP appropriées :

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            ex.getParams(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            ex.getParams(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            ex.getParams(),
            HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

### Traduction côté Frontend

Le frontend reçoit le code d'erreur et les paramètres, puis les traduit selon la langue de l'utilisateur :

**Réponse API** :
```json
{
  "errorCode": "deal.description.longueur",
  "params": [5000],
  "status": 400
}
```

**Traduction française** :
```json
{
  "errors": {
    "deal": {
      "description": {
        "longueur": "La description ne peut pas dépasser {{0}} caractères"
      }
    }
  }
}
```

**Traduction anglaise** :
```json
{
  "errors": {
    "deal": {
      "description": {
        "longueur": "Description cannot exceed {{0}} characters"
      }
    }
  }
}
```

### Checklist pour créer un nouveau Validator

- [ ] Créer `{Entité}Validator` dans `bff-core/domaine/validator/`
- [ ] Annoter avec `@Component`
- [ ] Définir les constantes de validation (MAX_LENGTH, MIN_LENGTH, etc.)
- [ ] Créer la méthode `valider({Entité}Modele)`
- [ ] Créer la méthode `validerPourMiseAJour({Entité}Modele)` si nécessaire
- [ ] Créer les méthodes de validation métier spécifiques
- [ ] Utiliser **uniquement** des `ValidationException` avec codes traduisibles
- [ ] Injecter le Validator dans le ServiceImpl correspondant
- [ ] Appeler le Validator dans **toutes** les méthodes du Service
- [ ] Documenter les codes d'erreur dans `CODES_ERREUR_TRADUISIBLES.md`

### Documentation complète

- **Liste complète des codes d'erreur** : `.github/documentation/CODES_ERREUR_TRADUISIBLES.md`
- **Environ 80+ codes d'erreur** couvrant toutes les entités

---

## 🖼️ Gestion des images avec MinIO

### Architecture complète
```
Frontend (React) ←→ Backend (BFF) ←→ MinIO Storage
      ↓                   ↓               ↓
  Sélection          Génération      Stockage
   images          presignUrl        images
      ↓                   ↓               ↓
  Upload direct ←--------┘               ↓
      ↓                                   ↓
  Confirmation -------→ Statut UPLOADED  ↓
```

### Principe
- Upload **direct** depuis le frontend vers MinIO via **URL présignées**
- Backend génère les URL présignées et gère les statuts des images
- Le **nom du fichier** est rendu unique avec un **timestamp** et placé dans un **répertoire par entité**
  - Format : `{entite}/{baseName}_{timestamp}.{extension}`
  - Exemple : `deals/unique_00011_1707988800000.png`
- Support multi-entités : Deal, Publicité, Utilisateur
- Répertoires définis dans `Tools.java` :
  - `DIRECTORY_DEALS_IMAGES = "deals/"`
  - `DIRECTORY_PUBLICITES_IMAGES = "publicites/"`
  - `DIRECTORY_UTILISATEUR_IMAGES = "utilisateurs/"`

### Statuts des images
```java
public enum StatutImage {
    PENDING,   // En attente d'upload (URL présignée générée)
    UPLOADED,  // Uploadé avec succès sur MinIO
    FAILED     // Échec de l'upload
}
```

### Flux d'upload complet

#### Backend (création avec images)
1. **Frontend** envoie les métadonnées des images (urlImage, nomUnique, isPrincipal)
   - `urlImage` : nom original du fichier (ex: `image.jpg`)
   - `nomUnique` : nom proposé par le frontend (ex: `unique_00011.png`)
   - `isPrincipal` : boolean indiquant l'image principale
2. **Backend** crée l'entité avec statut `PENDING` pour chaque image
3. **Backend** génère le **nom complet unique** avec timestamp :
   - Format : `{répertoire}{baseName}_{timestamp}.{extension}`
   - Utilise `FilenameUtils` d'Apache Commons IO
   - Exemple : `deals/unique_00011_1707988800000.png`
4. **Backend** génère les **presignUrl** (PUT, validité configurée dans `presignedUrlExpiry`)
5. **Backend** retourne l'entité avec les `presignUrl` et `nomUnique`

#### Frontend (upload)
6. **Frontend** extrait le **répertoire** et le **nom de fichier** depuis `nomUnique`
   - Parsing de `deals/unique_00011_1707988800000.png`
   - Répertoire : `deals/`
   - Nom du fichier : `unique_00011_1707988800000.png`
7. **Frontend** upload chaque image directement vers MinIO via `presignUrl` avec méthode PUT
8. **Frontend** appelle `PATCH /{entityUuid}/images/{imageUuid}/confirm` pour chaque image uploadée
9. **Backend** met à jour le statut en `UPLOADED`

#### Lecture
10. **Frontend** récupère les images avec `GET /{entityUuid}`
11. **Backend** génère automatiquement les `presignUrl` (GET) pour images avec statut `PENDING`
12. **Frontend** affiche les images via les URLs présignées

### FileManager (bff-provider/utils)

```java
@Component
public class FileManager {
    
    @Value("${minio.bucket.name}")
    private String bucketName;
    
    @Value("${minio.presigned.url.expiration}")
    private int presignedUrlExpiry; // Durée de validité en secondes

    @Autowired
    private MinioClient minioClient;

    /**
     * Génère URL présignée pour UPLOAD (méthode PUT)
     * @param folderName répertoire dans MinIO (ex: "deals/")
     * @param uniqueFileName nom complet du fichier avec timestamp
     * @return URL présignée pour upload
     */
    public String generatePresignedUrl(String folderName, String uniqueFileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(folderName.concat(uniqueFileName))
                    .expiry(presignedUrlExpiry)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération URL présignée: " + e.getMessage(), e);
        }
    }

    /**
     * Génère URL présignée pour LECTURE (méthode GET)
     * @param fullFileName chemin complet (ex: "deals/unique_00011_1707988800000.png")
     * @return URL présignée pour lecture
     */
    public String generatePresignedUrlForRead(String fullFileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(fullFileName)
                    .expiry(presignedUrlExpiry)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération URL lecture: " + e.getMessage(), e);
        }
    }
    
    /**
     * Upload un fichier vers MinIO
     * @param inputStream flux d'entrée du fichier
     * @param uniqueFileName nom unique du fichier
     * @param folderName répertoire de destination
     * @param size taille du fichier
     */
    public void uploadMinioFile(InputStream inputStream, String uniqueFileName, 
                                String folderName, long size) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(uniqueFileName);
        
        try {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .contentType(mimeType)
                .object(folderName + "/" + uniqueFileName)
                .stream(inputStream, size, -1)
                .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur upload MinIO: " + e.getMessage(), e);
        }
    }
}
```

### Pattern dans ProviderAdapter

#### Méthode sauvegarder() - Deal avec images
```java
@Component
@RequiredArgsConstructor
public class DealProviderAdapter implements DealProvider {
    
    private final DealRepository jpaRepository;
    private final DealJpaMapper mapper;
    private final FileManager fileManager;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele sauvegarder(DealModele deal) {
        DealJpa entite = mapper.versEntite(deal);
        
        // 1. Générer noms uniques avec timestamp pour chaque image
        if (deal.getListeImages() != null && !deal.getListeImages().isEmpty()) {
            List<ImageDealJpa> imageDealJpas = deal.getListeImages().stream()
                .map(imageDealModele -> ImageDealJpa.builder()
                    .uuid(imageDealModele.getUuid())
                    .urlImage(Tools.DIRECTORY_DEALS_IMAGES
                        + FilenameUtils.getBaseName(imageDealModele.getUrlImage())
                        + "_" + System.currentTimeMillis()
                        + "." + FilenameUtils.getExtension(imageDealModele.getUrlImage()))
                    .isPrincipal(imageDealModele.getIsPrincipal())
                    .statut(imageDealModele.getStatut())
                    .dealJpa(entite)
                    .build())
                .toList();
            entite.setImageDealJpas(imageDealJpas);
        }
        
        // 2. Sauvegarder en base de données
        DealJpa sauvegarde = jpaRepository.save(entite);
        DealModele modele = mapper.versModele(sauvegarde);
        
        // 3. Générer URL présignées pour images PENDING
        setPresignUrl(modele);
        
        return modele;
    }

    private void setPresignUrl(DealModele modele) {
        if (modele.getListeImages() != null && !modele.getListeImages().isEmpty()) {
            modele.getListeImages().stream()
                .filter(img -> img.getStatut() == StatutImage.PENDING)
                .forEach(img -> {
                    // Génère URL présignée en passant le répertoire et le nom complet
                    String presignUrl = fileManager.generatePresignedUrl(
                        Tools.DIRECTORY_DEALS_IMAGES, 
                        img.getUrlImage()
                    );
                    img.setPresignUrl(presignUrl);
                });
        }
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        DealJpa entite = jpaRepository.findById(uuid)
            .map(jpa -> {
                mapper.mettreAJour(jpa, deal);
                mettreAJourImagesSiBesoin(jpa, deal);
                return jpaRepository.save(jpa);
            })
            .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé : " + uuid));
            
        DealModele modeleSauvegarde = mapper.versModele(entite);
        setPresignUrl(modeleSauvegarde);
        return modeleSauvegarde;
    }

    private void mettreAJourImagesSiBesoin(DealJpa jpa, DealModele deal) {
        if (jpa.getImageDealJpas() == null || jpa.getImageDealJpas().isEmpty()) {
            return;
        }
        if (deal.getListeImages() == null || deal.getListeImages().isEmpty()) {
            return;
        }

        var imagesParUuid = deal.getListeImages().stream()
            .filter(image -> image.getUuid() != null)
            .collect(Collectors.toMap(
                image -> image.getUuid(),
                image -> image,
                (image1, image2) -> image2
            ));

        // Si URL modifiée : ajouter timestamp et repasser en PENDING
        jpa.getImageDealJpas().forEach(imageJpa -> {
            var imageEntrante = imagesParUuid.get(imageJpa.getUuid());
            if (imageEntrante == null) {
                return;
            }

            String urlEntrante = imageEntrante.getUrlImage();
            String urlActuelle = imageJpa.getUrlImage();

            if (urlEntrante != null && !urlEntrante.equals(urlActuelle)) {
                String nouvelleUrl = urlEntrante + "_" + System.currentTimeMillis();
                imageJpa.setUrlImage(nouvelleUrl);
                imageJpa.setStatut(StatutImage.PENDING);
                imageJpa.setDateModification(LocalDateTime.now());
            }
        });
    }

    @Override
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        DealJpa deal = jpaRepository.findById(dealUuid)
            .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé : " + dealUuid));
            
        deal.getImageDealJpas().stream()
            .filter(img -> img.getUuid().equals(imageUuid))
            .findFirst()
            .ifPresentOrElse(
                img -> {
                    img.setStatut(statut);
                    img.setDateModification(LocalDateTime.now());
                    jpaRepository.save(deal);
                },
                () -> {
                    throw new IllegalArgumentException("Image non trouvée : " + imageUuid);
                }
            );
    }

    @Override
    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        DealJpa deal = jpaRepository.findById(dealUuid)
            .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé : " + dealUuid));
            
        return deal.getImageDealJpas().stream()
            .filter(img -> img.getUuid().equals(imageUuid))
            .findFirst()
            .map(img -> fileManager.generatePresignedUrlForRead(img.getUrlImage()))
            .orElseThrow(() -> new IllegalArgumentException("Image non trouvée : " + imageUuid));
    }
}
```

### Endpoints requis dans Resource

```java
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealResource {
    
    private final DealApiAdapter apiAdapter;

    /**
     * Créer un nouveau deal avec images
     * Le frontend envoie les métadonnées des images (urlImage, nomUnique, isPrincipal)
     * Le backend répond avec les presignUrl pour chaque image
     */
    @PostMapping
    public ResponseEntity<DealResponseDto> creer(@RequestBody DealDTO dto) {
        log.info("Création d'un deal: {}", dto.getTitre());
        DealResponseDto deal = apiAdapter.creerDeal(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deal);
    }

    /**
     * Confirmer l'upload d'une image (PENDING → UPLOADED)
     * Endpoint appelé par le frontend après upload réussi vers MinIO
     */
    @PatchMapping("/{dealUuid}/images/{imageUuid}/confirm")
    public ResponseEntity<Void> confirmerUploadImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        log.info("Confirmation upload image {} pour deal {}", imageUuid, dealUuid);
        
        try {
            apiAdapter.mettreAJourStatutImage(
                dealUuid,
                imageUuid,
                StatutImage.UPLOADED
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur confirmation upload: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtenir l'URL de lecture d'une image
     * Génère une URL présignée pour lire l'image depuis MinIO
     */
    @GetMapping("/{dealUuid}/images/{imageUuid}/url")
    public ResponseEntity<Map<String, String>> obtenirUrlImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        log.debug("Récupération URL lecture image {} du deal {}", imageUuid, dealUuid);
        
        try {
            String urlLecture = apiAdapter.obtenirUrlLectureImage(dealUuid, imageUuid);
            return ResponseEntity.ok(Map.of("url", urlLecture));
        } catch (IllegalArgumentException e) {
            log.error("Erreur récupération URL: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
```

### Frontend React - Intégration complète

#### 1. Service d'upload d'images
```typescript
// src/common/api/imageService.ts
export const imageService = {
  /**
   * Upload une image vers MinIO via URL présignée
   * @param presignUrl URL présignée générée par le backend
   * @param file Fichier à uploader
   * @param onProgress Callback pour suivre la progression
   */
  uploadToMinio: async (
    presignUrl: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<void> => {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      
      // Suivi de progression
      xhr.upload.addEventListener("progress", (e) => {
        if (e.lengthComputable && onProgress) {
          const percent = Math.round((e.loaded / e.total) * 100);
          onProgress(percent);
        }
      });

      xhr.addEventListener("load", () => {
        if (xhr.status === 200) {
          resolve();
        } else {
          reject(new Error(`Upload échoué: ${xhr.status}`));
        }
      });

      xhr.addEventListener("error", () => {
        reject(new Error("Erreur réseau lors de l'upload"));
      });

      // Upload vers MinIO avec méthode PUT
      xhr.open("PUT", presignUrl);
      xhr.setRequestHeader("Content-Type", file.type);
      xhr.send(file);
    });
  },

  /**
   * Confirme l'upload d'une image auprès du backend
   */
  confirmUpload: async (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    imageUuid: string
  ): Promise<void> => {
    const response = await fetch(
      `/api/${entityType}/${entityUuid}/images/${imageUuid}/confirm`,
      {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
      }
    );

    if (!response.ok) {
      throw new Error("Échec de la confirmation d'upload");
    }
  },
};
```

#### 2. Hook useImageUpload
```typescript
// src/common/api/hooks/useImageUpload.ts
export const useImageUpload = () => {
  const [progress, setProgress] = useState<Map<string, UploadProgress>>(new Map());
  const [isUploading, setIsUploading] = useState(false);
  const [hasErrors, setHasErrors] = useState(false);

  const uploadImages = async (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    images: ImageResponse[],
    files: File[],
  ) => {
    setIsUploading(true);
    setProgress(new Map());

    const uploadPromises = images.map(async (imageResponse) => {
      try {
        // Trouver le fichier correspondant
        const file = files.find(f => {
          const baseName = f.name.split('.')[0];
          return imageResponse.nomUnique?.includes(baseName);
        });

        if (!file) {
          throw new Error(`Fichier non trouvé pour ${imageResponse.nomUnique}`);
        }

        // 1. Upload vers MinIO via URL présignée
        await imageService.uploadToMinio(
          imageResponse.presignUrl,
          file,
          (progressPercent) => updateProgress(imageResponse.uuid, { progress: progressPercent })
        );

        // 2. Confirmer au backend
        await imageService.confirmUpload(entityType, entityUuid, imageResponse.uuid);
        
        updateProgress(imageResponse.uuid, { status: "success" });
      } catch (error) {
        updateProgress(imageResponse.uuid, { 
          status: "error", 
          error: error.message 
        });
        setHasErrors(true);
      }
    });

    await Promise.all(uploadPromises);
    setIsUploading(false);
  };

  return { uploadImages, progress, isUploading, hasErrors };
};
```

#### 3. Hook useCreateDeal avec upload automatique
```typescript
export const useCreateDeal = () => {
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<DealDTO, Error, CreateDealDTO>({
    mutationFn: async (input) => {
      // 1. Créer le deal avec métadonnées images
      const payload = {
        ...input,
        listeImages: input.listeImages.map((img) => ({
          urlImage: img.urlImage,
          nomUnique: img.nomUnique,
          statut: null,
          isPrincipal: img.isPrincipal,
        })),
      };

      const dealCree = await apiClient.post<DealDTO>("/deals", { body: payload });

      // 2. Uploader les images vers MinIO + confirmer
      const imagesFromBackend = dealCree.listeImages ?? [];
      if (imagesFromBackend.length > 0) {
        const filesForUpload = input.listeImages.map(img => img.file);
        await uploadImages("deals", dealCree.uuid, imagesFromBackend, filesForUpload);
      }

      return dealCree;
    },
  });

  return { ...mutation, progress, isUploading, hasErrors };
};
```

#### 4. Composant CreateDealModal avec progression
```tsx
function CreateDealModal() {
  const { mutateAsync: createDeal, isUploading, progress } = useCreateDeal();
  const [images, setImages] = useState<File[]>([]);

  const handleSubmit = async (data) => {
    const payload = {
      ...data,
      listeImages: images.map((file, idx) => ({
        urlImage: file.name,
        nomUnique: `unique_${String(idx).padStart(5, '0')}.${file.name.split('.').pop()}`,
        isPrincipal: idx === 0, // Première image = principale
        file, // Pour upload ultérieur
      })),
    };

    await createDeal(payload);
  };

  return (
    <Dialog>
      {/* Indicateur de progression */}
      {isUploading && <UploadProgress progress={progress} />}
      
      <Form onSubmit={handleSubmit}>
        <ImageUploader
          maxImages={5}
          onChange={setImages}
          accept="image/jpeg,image/png"
        />
      </Form>
    </Dialog>
  );
}
```

#### 5. Parsing du nomUnique pour upload (IMPORTANT)
```typescript
/**
 * Parse le nomUnique reçu du backend pour extraire le répertoire et le nom
 * Exemple: "deals/unique_00011_1707988800000.png"
 * Retourne: { directory: "deals/", filename: "unique_00011_1707988800000.png" }
 */
function parseImagePath(nomUnique: string): { directory: string; filename: string } {
  const lastSlashIndex = nomUnique.lastIndexOf('/');
  
  if (lastSlashIndex === -1) {
    return { directory: '', filename: nomUnique };
  }
  
  return {
    directory: nomUnique.substring(0, lastSlashIndex + 1),
    filename: nomUnique.substring(lastSlashIndex + 1)
  };
}

// Utilisation lors de l'upload
const { directory, filename } = parseImagePath(imageResponse.nomUnique);
// Le presignUrl contient déjà le chemin complet, pas besoin de reconstruire
await imageService.uploadToMinio(imageResponse.presignUrl, file);
```

### Méthodes à implémenter dans Provider
```java
// CRUD standard
DealModele sauvegarder(DealModele deal);
DealModele mettreAJour(UUID uuid, DealModele deal);
Optional<DealModele> trouverParUuid(UUID uuid);
List<DealModele> trouverTous();
void supprimerParUuid(UUID uuid);

// Gestion des images (obligatoire si l'entité a des images)
void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut);
String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid);
```

### Points clés à retenir

1. ✅ **Format du nom complet** : `{répertoire}/{baseName}_{timestamp}.{extension}`
   - Utiliser `FilenameUtils.getBaseName()` et `FilenameUtils.getExtension()`
   - Exemple : `deals/unique_00011_1707988800000.png`

2. ✅ **Répertoires définis** : Toujours utiliser les constantes de `Tools.java`
   - `Tools.DIRECTORY_DEALS_IMAGES` = `"deals/"`
   - `Tools.DIRECTORY_PUBLICITES_IMAGES` = `"publicites/"`
   - `Tools.DIRECTORY_UTILISATEUR_IMAGES` = `"utilisateurs/"`

3. ✅ **Statut PENDING** : État initial pour toute nouvelle image

4. ✅ **URL présignées** : 
   - Upload : `generatePresignedUrl(folderName, uniqueFileName)` avec méthode PUT
   - Lecture : `generatePresignedUrlForRead(fullFileName)` avec méthode GET
   - Générées automatiquement pour images PENDING dans `setPresignUrl()`

5. ✅ **Upload direct** : Frontend → MinIO (pas de proxy backend)
   - Utilise XMLHttpRequest avec méthode PUT
   - Content-Type doit correspondre au type du fichier

6. ✅ **Confirmation** : Frontend doit appeler l'endpoint PATCH `/confirm` après upload réussi

7. ✅ **Image principale** : Première image du tableau (`isPrincipal = true`)

8. ✅ **FileManager** : Toujours utiliser pour interaction avec MinIO
   - Injecté via `@RequiredArgsConstructor` dans le ProviderAdapter
   - Gère toutes les opérations MinIO (upload, URL présignées, etc.)

9. ✅ **Modification détectée** : Si URL change → nouveau timestamp + PENDING
   - Dans `mettreAJourImagesSiBesoin()` du ProviderAdapter

10. ✅ **Timestamp unique** : `System.currentTimeMillis()` garantit l'unicité du nom

11. ✅ **Gestion des erreurs** : 
    - Backend : `IllegalArgumentException` avec message descriptif
    - Frontend : Try/catch avec gestion du statut FAILED

12. ✅ **Parsing du nomUnique** : Frontend doit extraire répertoire et nom de fichier
    - Format reçu : `deals/unique_00011_1707988800000.png`
    - presignUrl contient déjà le chemin complet

### Documentation complète
- Backend : `.github/documentation/GESTION_IMAGES_MINIO.md`
- Frontend : `.github/documentation/FRONTEND_UPLOAD_IMAGES_REACT.md`
- **Instruction complète** : `.github/instructions/GESTION_IMAGES_MINIO_INSTRUCTION.md`

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
- [ ] **Créer `{Entité}Validator` dans `domaine/validator/` (OBLIGATOIRE)**
  - [ ] Méthode `valider({Entité}Modele)` avec toutes les règles métier
  - [ ] Méthode `validerPourMiseAJour({Entité}Modele)` si nécessaire
  - [ ] Méthodes de validation métier spécifiques (transitions d'état, etc.)
  - [ ] Utiliser **uniquement** `ValidationException` avec codes traduisibles
- [ ] Créer interface `{Entité}Provider` dans `provider/`
- [ ] Créer interface `{Entité}Service` dans `domaine/service/`
- [ ] Créer `{Entité}ServiceImpl` dans `domaine/impl/`
  - [ ] Injecter le `{Entité}Validator`
  - [ ] Appeler `validator.valider()` dans TOUTES les méthodes métier
- [ ] **Documenter les codes d'erreur dans `CODES_ERREUR_TRADUISIBLES.md`**

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

### Architecture et structure
1. ✅ **Toujours** respecter l'architecture hexagonale
2. ✅ **Jamais** de dépendance technique dans bff-core
3. ✅ **Toujours** utiliser des suffixes explicites (`Modele`, `Jpa`, `DTO`)
4. ✅ **Toujours** mapper entre les couches (ne pas exposer les entités JPA)

### Validation et Exceptions
5. ✅ **TOUTES les règles métier doivent être dans les Validators** (bff-core/domaine/validator/)
6. ✅ **TOUJOURS** valider dans le Service avant d'appeler le Provider
7. ✅ **TOUJOURS** utiliser des exceptions avec codes d'erreur traduisibles (ValidationException, ResourceNotFoundException, etc.)
8. ✅ **JAMAIS** utiliser IllegalArgumentException ou RuntimeException directement
9. ✅ **TOUJOURS** créer un Validator pour chaque entité métier
10. ✅ **TOUJOURS** documenter les nouveaux codes d'erreur dans CODES_ERREUR_TRADUISIBLES.md

### Gestion des images (MinIO)
11. ✅ **Toujours** générer les URL présignées pour images avec statut PENDING
12. ✅ **Toujours** ajouter timestamp unique aux noms de fichiers
13. ✅ **Toujours** utiliser FileManager pour MinIO
14. ✅ **Toujours** suivre le pattern : Frontend → MinIO (direct) → Backend (confirmation)

### Tests et Documentation
15. ✅ **Toujours** créer les tests unitaires (minimum 10+ par ServiceImpl)
16. ✅ **Toujours** documenter les endpoints dans fichiers .http
17. ✅ **Toujours** demander confirmation avant de créer de la documentation

### Codes d'erreur traduisibles
18. ✅ **Format** : `{entité}.{attribut}.{type}` (ex: `deal.titre.obligatoire`)
19. ✅ **Avec paramètres** : `new ValidationException("deal.description.longueur", 5000)`
20. ✅ **ResourceNotFoundException** : `ResourceNotFoundException.parUuid("deal", uuid)`

---

**Date de dernière mise à jour** : 28 février 2026  
**Auteur** : Équipe PayToGether
