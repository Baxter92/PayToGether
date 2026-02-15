# 🖼️ Instruction - Gestion des Images avec MinIO

## 📋 Vue d'ensemble

Cette instruction décrit **comment implémenter la gestion des images** dans le projet PayToGether en utilisant **MinIO** comme stockage d'objets, avec une architecture d'**upload direct** via URL présignées.

---

## 🎯 Principe de fonctionnement

### Architecture
```
Frontend (React) ──────────────► MinIO (Upload direct)
      │                               │
      │                               │
      ▼                               ▼
  Backend (BFF) ───────────────► PostgreSQL
  Génère presignUrl            Stocke métadonnées
```

### Flux complet

1. **Frontend** : Utilisateur sélectionne des images
2. **Frontend → Backend** : Envoie métadonnées images (urlImage, nomUnique, isPrincipal)
3. **Backend** : Génère nom unique avec timestamp + URL présignée
4. **Backend → Frontend** : Retourne entité avec presignUrl pour chaque image
5. **Frontend → MinIO** : Upload direct via presignUrl (méthode PUT)
6. **Frontend → Backend** : Confirmation upload (endpoint PATCH `/confirm`)
7. **Backend** : Met à jour statut PENDING → UPLOADED

---

## 🏗️ Structure du nom de fichier

### Format
```
{répertoire}/{baseName}_{timestamp}.{extension}
```

### Exemples
- Deal : `deals/unique_00011_1707988800000.png`
- Publicité : `publicites/promo_winter_1707988800000.jpg`
- Utilisateur : `utilisateurs/avatar_john_1707988800000.jpeg`

### Composition
1. **Répertoire** : Défini dans `Tools.java`
   - `Tools.DIRECTORY_DEALS_IMAGES` = `"deals/"`
   - `Tools.DIRECTORY_PUBLICITES_IMAGES` = `"publicites/"`
   - `Tools.DIRECTORY_UTILISATEUR_IMAGES` = `"utilisateurs/"`

2. **Base name** : Nom du fichier sans extension
   - Extrait avec `FilenameUtils.getBaseName(urlImage)`

3. **Timestamp** : `System.currentTimeMillis()`
   - Garantit l'unicité du fichier

4. **Extension** : Extension originale du fichier
   - Extraite avec `FilenameUtils.getExtension(urlImage)`

---

## 🔧 Implémentation Backend

### 1. Dépendances Maven

```xml
<!-- pom.xml du module bff-provider -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>

<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.15.1</version>
</dependency>
```

### 2. Configuration application.yml

```yaml
minio:
  endpoint: http://minio:9000
  bucket:
    name: paytogether-images
  presigned:
    url:
      expiration: 3600  # 1 heure en secondes
```

### 3. FileManager (bff-provider/utils)

```java
@Component
public class FileManager {
    
    @Value("${minio.bucket.name}")
    private String bucketName;
    
    @Value("${minio.presigned.url.expiration}")
    private int presignedUrlExpiry;

    @Autowired
    private MinioClient minioClient;

    /**
     * Génère URL présignée pour UPLOAD (méthode PUT)
     * @param folderName répertoire dans MinIO (ex: "deals/")
     * @param uniqueFileName nom complet du fichier avec timestamp
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
}
```

### 4. ProviderAdapter - Méthode sauvegarder()

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
                    .statut(StatutImage.PENDING)
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
                    String presignUrl = fileManager.generatePresignedUrl(
                        Tools.DIRECTORY_DEALS_IMAGES, 
                        img.getUrlImage()
                    );
                    img.setPresignUrl(presignUrl);
                });
        }
    }
}
```

### 5. ProviderAdapter - Méthode mettreAJour()

```java
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
            String nouvelleUrl = Tools.DIRECTORY_DEALS_IMAGES
                + FilenameUtils.getBaseName(urlEntrante)
                + "_" + System.currentTimeMillis()
                + "." + FilenameUtils.getExtension(urlEntrante);
            imageJpa.setUrlImage(nouvelleUrl);
            imageJpa.setStatut(StatutImage.PENDING);
            imageJpa.setDateModification(LocalDateTime.now());
        }
    });
}
```

### 6. ProviderAdapter - Méthodes de gestion des images

```java
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
```

### 7. Provider - Interface (bff-core)

```java
public interface DealProvider {
    // CRUD standard
    DealModele sauvegarder(DealModele deal);
    DealModele mettreAJour(UUID uuid, DealModele deal);
    Optional<DealModele> trouverParUuid(UUID uuid);
    List<DealModele> trouverTous();
    void supprimerParUuid(UUID uuid);
    
    // Gestion des images (obligatoire si l'entité a des images)
    void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut);
    String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid);
}
```

### 8. Resource - Endpoints (bff-api)

```java
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealResource {
    
    private final DealApiAdapter apiAdapter;

    /**
     * Créer un nouveau deal avec images
     */
    @PostMapping
    public ResponseEntity<DealResponseDto> creer(@RequestBody DealDTO dto) {
        log.info("Création d'un deal: {}", dto.getTitre());
        DealResponseDto deal = apiAdapter.creerDeal(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(deal);
    }

    /**
     * Confirmer l'upload d'une image (PENDING → UPLOADED)
     */
    @PatchMapping("/{dealUuid}/images/{imageUuid}/confirm")
    public ResponseEntity<Void> confirmerUploadImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        log.info("Confirmation upload image {} pour deal {}", imageUuid, dealUuid);
        
        try {
            apiAdapter.mettreAJourStatutImage(dealUuid, imageUuid, StatutImage.UPLOADED);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur confirmation upload: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtenir l'URL de lecture d'une image
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

### 9. ApiAdapter - Délégation (bff-api)

```java
@Component
@RequiredArgsConstructor
public class DealApiAdapter {
    
    private final DealService service;
    
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        service.mettreAJourStatutImage(dealUuid, imageUuid, statut);
    }
    
    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        return service.obtenirUrlLectureImage(dealUuid, imageUuid);
    }
}
```

### 10. Service - Délégation (bff-core)

```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    
    private final DealProvider provider;
    
    @Override
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        provider.mettreAJourStatutImage(dealUuid, imageUuid, statut);
    }
    
    @Override
    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        return provider.obtenirUrlLectureImage(dealUuid, imageUuid);
    }
}
```

---

## 🎨 Implémentation Frontend (React/TypeScript)

### 1. Service d'upload d'images

```typescript
// src/common/api/imageService.ts
export const imageService = {
  /**
   * Upload une image vers MinIO via URL présignée
   */
  uploadToMinio: async (
    presignUrl: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<void> => {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      
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

### 2. Hook useImageUpload

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

### 3. Utilisation dans un composant

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

      const dealCree = await apiClient.post<DealDTO>("/api/deals", { body: payload });

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

### 4. Composant avec sélection d'images

```tsx
function CreateDealModal() {
  const { mutateAsync: createDeal, isUploading, progress } = useCreateDeal();
  const [images, setImages] = useState<File[]>([]);

  const handleSubmit = async (data: FormData) => {
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
      <Form onSubmit={handleSubmit}>
        <ImageUploader
          maxImages={5}
          onChange={setImages}
          accept="image/jpeg,image/png"
        />
        
        {isUploading && (
          <div>
            {Array.from(progress.entries()).map(([uuid, prog]) => (
              <ProgressBar key={uuid} value={prog.progress} />
            ))}
          </div>
        )}
      </Form>
    </Dialog>
  );
}
```

---

## 📊 Statuts des images

```java
public enum StatutImage {
    PENDING,   // En attente d'upload (URL présignée générée)
    UPLOADED,  // Uploadé avec succès sur MinIO
    FAILED     // Échec de l'upload
}
```

### Transitions de statuts

```
PENDING ──upload réussi──► UPLOADED
   │
   │
   └────upload échoué────► FAILED
```

---

## ✅ Checklist d'implémentation

### Backend
- [ ] Ajouter dépendances MinIO et Commons IO dans pom.xml
- [ ] Configurer MinIO dans application.yml
- [ ] Créer/vérifier FileManager avec méthodes `generatePresignedUrl()` et `generatePresignedUrlForRead()`
- [ ] Ajouter constante répertoire dans Tools.java
- [ ] Implémenter `sauvegarder()` avec génération nom unique
- [ ] Implémenter `mettreAJour()` avec `mettreAJourImagesSiBesoin()`
- [ ] Créer méthode `setPresignUrl()` pour générer URL présignées
- [ ] Implémenter `mettreAJourStatutImage()`
- [ ] Implémenter `obtenirUrlLectureImage()`
- [ ] Ajouter endpoints dans Resource : POST, PATCH `/confirm`, GET `/url`
- [ ] Déléguer dans ApiAdapter et Service

### Frontend
- [ ] Créer service `imageService` avec `uploadToMinio()` et `confirmUpload()`
- [ ] Créer hook `useImageUpload()` avec gestion de progression
- [ ] Créer hook `useCreate{Entity}()` utilisant `useImageUpload()`
- [ ] Implémenter composant de sélection d'images
- [ ] Ajouter indicateur de progression d'upload
- [ ] Gérer les erreurs d'upload
- [ ] Tester l'upload complet

### Tests
- [ ] Tester génération URL présignées
- [ ] Tester upload direct vers MinIO
- [ ] Tester confirmation d'upload
- [ ] Tester modification d'image (nouveau timestamp)
- [ ] Tester gestion des erreurs

---

## 🚨 Points d'attention

1. **Timestamp** : Ne jamais oublier d'ajouter le timestamp dans `sauvegarder()` et `mettreAJourImagesSiBesoin()`

2. **FilenameUtils** : Toujours utiliser Apache Commons IO pour manipuler les noms de fichiers

3. **Répertoire** : Utiliser les constantes de `Tools.java`, jamais de chaîne en dur

4. **Statut PENDING** : Nouvelle image = toujours PENDING, même si déjà uploadée

5. **presignUrl** : Généré uniquement pour images PENDING dans la réponse

6. **Frontend** : Upload avec méthode PUT, confirmation avec PATCH

7. **Erreurs** : Toujours lancer `IllegalArgumentException` avec message descriptif

8. **Transactions** : Méthodes `sauvegarder()` et `mettreAJour()` doivent être `@Transactional`

---

## 📚 Références

- Documentation MinIO : https://min.io/docs/minio/linux/developers/java/API.html
- Apache Commons IO : https://commons.apache.org/proper/commons-io/
- Documentation complète : `.github/documentation/GESTION_IMAGES_MINIO.md`
- Exemples frontend : `.github/documentation/FRONTEND_UPLOAD_IMAGES_REACT.md`

---

**Date de dernière mise à jour** : 15 février 2026  
**Auteur** : Équipe PayToGether

