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
- Le **nom du fichier** est rendu unique avec un **timestamp** (ex: `image.jpg_1707988800000`)
- Support multi-entités : Deal, Publicité, Utilisateur

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
1. **Frontend** envoie les métadonnées des images (nom, isPrincipal)
2. **Backend** crée l'entité avec statut `PENDING` pour chaque image
3. **Backend** ajoute un **timestamp unique** au nom de fichier
4. **Backend** génère les **presignUrl** (PUT, validité 1h)
5. **Backend** retourne l'entité avec les `presignUrl`

#### Frontend (upload)
6. **Frontend** upload chaque image directement vers MinIO via `presignUrl`
7. **Frontend** appelle `PATCH /{entityUuid}/images/{imageUuid}/confirm` pour chaque image uploadée
8. **Backend** met à jour le statut en `UPLOADED`

#### Lecture
9. **Frontend** récupère les images avec `GET /{entityUuid}`
10. **Backend** génère automatiquement les `presignUrl` pour images `PENDING` (lecture)
11. **Frontend** affiche les images via les URLs présignées

### FileManager (bff-provider/utils)

```java
@Component
@RequiredArgsConstructor
public class FileManager {
    private final MinioClient minioClient;
    
    @Value("${minio.bucketName}")
    private String bucketName;
    
    @Value("${minio.presignedUrlExpiration:3600}")
    private int presignedUrlExpiration; // 1 heure par défaut

    /**
     * Génère URL présignée pour UPLOAD (méthode PUT)
     * @param fullFileName nom complet avec timestamp (ex: image.jpg_1707988800000)
     */
    public String generatePresignedUrl(String fullFileName) {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucketName)
                .object(fullFileName)
                .expiry(presignedUrlExpiration)
                .build()
        );
    }

    /**
     * Génère URL présignée pour LECTURE (méthode GET)
     */
    public String generatePresignedUrlForRead(String fullFileName) {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(fullFileName)
                .expiry(presignedUrlExpiration)
                .build()
        );
    }
    
    /**
     * Supprime un fichier du bucket MinIO
     */
    public void supprimerFichier(String fullFileName) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fullFileName)
                .build()
        );
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

    @Override
    public DealModele sauvegarder(DealModele deal) {
        DealJpa entite = mapper.versEntite(deal);
        
        // 1. Ajouter timestamp unique aux noms d'images
        if (deal.getListeImages() != null && !deal.getListeImages().isEmpty()) {
            List<ImageDealJpa> imageDealJpas = deal.getListeImages().stream()
                .map(imageDealModele -> ImageDealJpa.builder()
                    .uuid(imageDealModele.getUuid())
                    .urlImage(imageDealModele.getUrlImage() + "_" + System.currentTimeMillis())
                    .isPrincipal(imageDealModele.getIsPrincipal())
                    .statut(StatutImageDeal.PENDING)
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
                .filter(img -> img.getStatut() == StatutImageDeal.PENDING)
                .forEach(img -> {
                    String presignUrl = fileManager.generatePresignedUrl(img.getUrlImage());
                    img.setPresignUrl(presignUrl);
                });
        }
    }

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
                ImageDealModele::getUuid,
                image -> image
            ));

        // Si URL modifiée : ajouter timestamp et repasser en PENDING
        jpa.getImageDealJpas().forEach(imageJpa -> {
            var imageModele = imagesParUuid.get(imageJpa.getUuid());
            if (imageModele != null && !imageJpa.getUrlImage().equals(imageModele.getUrlImage())) {
                imageJpa.setUrlImage(imageModele.getUrlImage() + "_" + System.currentTimeMillis());
                imageJpa.setStatut(StatutImageDeal.PENDING);
                imageJpa.setDateModification(LocalDateTime.now());
            }
        });
    }

    @Override
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImageDeal statut) {
        DealJpa deal = jpaRepository.findById(dealUuid)
            .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé : " + dealUuid));
            
        deal.getImageDealJpas().stream()
            .filter(img -> img.getUuid().equals(imageUuid))
            .findFirst()
            .ifPresent(img -> {
                img.setStatut(statut);
                img.setDateModification(LocalDateTime.now());
                jpaRepository.save(deal);
            });
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
@RequestMapping("/deals")
@RequiredArgsConstructor
public class DealResource {
    
    private final DealApiAdapter apiAdapter;

    /**
     * Confirmer l'upload d'une image (PENDING → UPLOADED)
     */
    @PatchMapping("/{dealUuid}/images/{imageUuid}/confirm")
    public ResponseEntity<Void> confirmerUploadImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        apiAdapter.confirmerUploadImage(dealUuid, imageUuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir l'URL de lecture d'une image
     */
    @GetMapping("/{dealUuid}/images/{imageUuid}/url")
    public ResponseEntity<Map<String, String>> obtenirUrlImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        String url = apiAdapter.obtenirUrlImage(dealUuid, imageUuid);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
```

### Frontend React - Intégration complète

#### 1. Hook useImageUpload
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
    files: ImageFile[],
  ) => {
    setIsUploading(true);
    setProgress(new Map());

    const uploadPromises = images.map(async (imageResponse) => {
      try {
        // 1. Upload vers MinIO
        await imageService.uploadToMinio(
          imageResponse.presignUrl,
          file,
          (progressPercent) => updateProgress(imageId, { progress: progressPercent })
        );

        // 2. Confirmer au backend
        await imageService.confirmUpload(entityType, entityUuid, imageResponse.uuid);
        
        updateProgress(imageId, { status: "success" });
      } catch (error) {
        updateProgress(imageId, { status: "error", error: error.message });
        setHasErrors(true);
      }
    });

    await Promise.all(uploadPromises);
    setIsUploading(false);
  };

  return { uploadImages, progress, isUploading, hasErrors };
};
```

#### 2. Hook useCreateDeal avec upload automatique
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
        const filesForUpload = /* préparer les fichiers */;
        await uploadImages("deals", dealCree.uuid, imagesFromBackend, filesForUpload);
      }

      return dealCree;
    },
  });

  return { ...mutation, progress, isUploading, hasErrors };
};
```

#### 3. Composant CreateDealModal avec progression
```tsx
function CreateDealModal() {
  const { mutateAsync: createDeal, isUploading, progress } = useCreateDeal();

  const handleSubmit = async (data) => {
    const payload = {
      ...data,
      listeImages: images.map((file, idx) => ({
        urlImage: file.name,
        nomUnique: file.name,
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
      
      <Form onSubmit={handleSubmit} />
    </Dialog>
  );
}
```

### Méthodes à implémenter dans Provider
```java
// CRUD standard
DealModele sauvegarder(DealModele deal);
DealModele mettreAJour(UUID uuid, DealModele deal);
Optional<DealModele> trouverParUuid(UUID uuid);
List<DealModele> trouverTous();
void supprimerParUuid(UUID uuid);

// Gestion des images
void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut);
String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid);
```

### Points clés à retenir

1. ✅ **Timestamp unique** : Toujours ajouter `System.currentTimeMillis()` au nom
2. ✅ **Statut PENDING** : État initial pour toute nouvelle image
3. ✅ **URL présignées** : Générées automatiquement pour images PENDING
4. ✅ **Upload direct** : Frontend → MinIO (pas de proxy backend)
5. ✅ **Confirmation** : Frontend doit appeler l'endpoint de confirmation
6. ✅ **Image principale** : Première image du tableau (`isPrincipal = true`)
7. ✅ **FileManager** : Toujours utiliser pour interaction avec MinIO
8. ✅ **Modification détectée** : Si URL change → nouveau timestamp + PENDING

### Documentation complète
- Backend : `.github/documentation/GESTION_IMAGES_MINIO.md`
- Frontend : `.github/documentation/GESTION_IMAGES_FRONTEND_UPLOAD.md`

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
