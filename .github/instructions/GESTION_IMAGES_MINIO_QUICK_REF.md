# 🎯 Quick Reference - Gestion des Images MinIO

Guide de référence rapide pour implémenter la gestion des images avec MinIO dans PayToGether.

---

## 📦 Format du nom de fichier

```
{répertoire}/{baseName}_{timestamp}.{extension}
```

### Exemples concrets
```
deals/unique_00011_1707988800000.png
publicites/promo_winter_1707988800000.jpg
utilisateurs/avatar_john_1707988800000.jpeg
```

---

## 🔄 Flux en 7 étapes

```
1. Frontend sélectionne images
   └─► urlImage: "image.jpg", nomUnique: "unique_00011.png"

2. Frontend envoie POST /api/deals
   └─► Body: { ..., listeImages: [{ urlImage, nomUnique, isPrincipal }] }

3. Backend génère nom unique avec timestamp
   └─► "deals/unique_00011_1707988800000.png"

4. Backend génère URL présignée (PUT)
   └─► presignUrl: "http://minio:9000/paytogether-images/deals/..."

5. Backend répond avec presignUrl + statut PENDING
   └─► { uuid, nomUnique: "deals/...", presignUrl, statut: "PENDING" }

6. Frontend upload vers MinIO via presignUrl (PUT)
   └─► xhr.open("PUT", presignUrl); xhr.send(file);

7. Frontend confirme PATCH /api/deals/{uuid}/images/{imageUuid}/confirm
   └─► Statut passe à UPLOADED
```

---

## 💻 Code Backend - ProviderAdapter

### Méthode sauvegarder()
```java
@Transactional(rollbackOn = Exception.class)
@Override
public DealModele sauvegarder(DealModele deal) {
    DealJpa entite = mapper.versEntite(deal);
    
    // Générer noms uniques avec timestamp
    if (deal.getListeImages() != null && !deal.getListeImages().isEmpty()) {
        List<ImageDealJpa> imageDealJpas = deal.getListeImages().stream()
            .map(img -> ImageDealJpa.builder()
                .uuid(img.getUuid())
                .urlImage(Tools.DIRECTORY_DEALS_IMAGES
                    + FilenameUtils.getBaseName(img.getUrlImage())
                    + "_" + System.currentTimeMillis()
                    + "." + FilenameUtils.getExtension(img.getUrlImage()))
                .isPrincipal(img.getIsPrincipal())
                .statut(StatutImage.PENDING)
                .dealJpa(entite)
                .build())
            .toList();
        entite.setImageDealJpas(imageDealJpas);
    }
    
    DealJpa sauvegarde = jpaRepository.save(entite);
    DealModele modele = mapper.versModele(sauvegarde);
    setPresignUrl(modele); // Génère URL présignées
    return modele;
}

private void setPresignUrl(DealModele modele) {
    if (modele.getListeImages() != null) {
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
```

### Endpoints requis
```java
// 1. Création avec images
@PostMapping
public ResponseEntity<DealResponseDto> creer(@RequestBody DealDTO dto)

// 2. Confirmation upload
@PatchMapping("/{dealUuid}/images/{imageUuid}/confirm")
public ResponseEntity<Void> confirmerUploadImage(
    @PathVariable UUID dealUuid,
    @PathVariable UUID imageUuid)

// 3. URL de lecture
@GetMapping("/{dealUuid}/images/{imageUuid}/url")
public ResponseEntity<Map<String, String>> obtenirUrlImage(
    @PathVariable UUID dealUuid,
    @PathVariable UUID imageUuid)
```

---

## 🎨 Code Frontend - React/TypeScript

### Service d'upload
```typescript
export const imageService = {
  uploadToMinio: async (
    presignUrl: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<void> => {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      
      xhr.upload.addEventListener("progress", (e) => {
        if (e.lengthComputable && onProgress) {
          onProgress(Math.round((e.loaded / e.total) * 100));
        }
      });

      xhr.addEventListener("load", () => {
        xhr.status === 200 ? resolve() : reject(new Error(`Upload échoué: ${xhr.status}`));
      });

      xhr.open("PUT", presignUrl);
      xhr.setRequestHeader("Content-Type", file.type);
      xhr.send(file);
    });
  },

  confirmUpload: async (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    imageUuid: string
  ) => {
    const response = await fetch(
      `/api/${entityType}/${entityUuid}/images/${imageUuid}/confirm`,
      { method: "PATCH" }
    );
    if (!response.ok) throw new Error("Échec confirmation");
  },
};
```

### Hook useImageUpload
```typescript
export const useImageUpload = () => {
  const uploadImages = async (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    images: ImageResponse[],
    files: File[],
  ) => {
    const uploadPromises = images.map(async (img) => {
      const file = files.find(f => img.nomUnique?.includes(f.name.split('.')[0]));
      
      // 1. Upload vers MinIO
      await imageService.uploadToMinio(img.presignUrl, file, onProgress);
      
      // 2. Confirmer au backend
      await imageService.confirmUpload(entityType, entityUuid, img.uuid);
    });

    await Promise.all(uploadPromises);
  };

  return { uploadImages };
};
```

### Utilisation dans un composant
```typescript
const { mutateAsync: createDeal } = useCreateDeal();

const handleSubmit = async (data: FormData) => {
  const payload = {
    ...data,
    listeImages: images.map((file, idx) => ({
      urlImage: file.name,
      nomUnique: `unique_${String(idx).padStart(5, '0')}.${file.name.split('.').pop()}`,
      isPrincipal: idx === 0,
      file,
    })),
  };

  await createDeal(payload);
};
```

---

## ✅ Checklist rapide

### Backend
- [ ] FileManager avec `generatePresignedUrl(folderName, uniqueFileName)`
- [ ] Constante répertoire dans Tools.java
- [ ] `sauvegarder()` : génération nom avec `FilenameUtils` + timestamp
- [ ] `setPresignUrl()` : génère URL pour images PENDING
- [ ] `mettreAJourStatutImage()` : PENDING → UPLOADED
- [ ] Endpoints : POST, PATCH `/confirm`, GET `/url`

### Frontend
- [ ] Service `uploadToMinio()` avec XMLHttpRequest PUT
- [ ] Service `confirmUpload()` avec PATCH
- [ ] Hook `useImageUpload()` avec gestion progression
- [ ] Composant sélection d'images
- [ ] Mapping `nomUnique` = `unique_${idx}.${ext}`

---

## 🚨 Pièges à éviter

| ❌ À ne pas faire | ✅ À faire |
|------------------|-----------|
| Oublier le timestamp | `FilenameUtils.getBaseName() + "_" + System.currentTimeMillis()` |
| Nom en dur : `"deals/"` | `Tools.DIRECTORY_DEALS_IMAGES` |
| Upload avec POST | Upload avec **PUT** vers presignUrl |
| Oublier confirmation | Toujours appeler PATCH `/confirm` après upload |
| Statut UPLOADED direct | Toujours créer en **PENDING**, puis UPLOADED après confirm |

---

## 🔑 Points clés

1. **Timestamp** = `System.currentTimeMillis()` → garantit unicité
2. **FilenameUtils** = Apache Commons IO → extraire base et extension
3. **Tools.DIRECTORY_XXX** = constantes pour répertoires
4. **StatutImage.PENDING** = état initial obligatoire
5. **generatePresignedUrl(folder, file)** = méthode PUT
6. **generatePresignedUrlForRead(fullPath)** = méthode GET
7. **setPresignUrl()** = appel automatique après sauvegarde
8. **XMLHttpRequest PUT** = upload direct vers MinIO
9. **PATCH /confirm** = confirmation obligatoire
10. **@Transactional** = sur sauvegarder() et mettreAJour()

---

## 📊 Statuts des images

```
┌─────────┐
│ PENDING │  État initial (URL présignée générée)
└────┬────┘
     │
     ├─── Upload réussi ────► UPLOADED
     │
     └─── Upload échoué ────► FAILED
```

---

## 📚 Documentation complète

- **Instruction complète** : `.github/instructions/GESTION_IMAGES_MINIO_INSTRUCTION.md`
- **Documentation backend** : `.github/documentation/GESTION_IMAGES_MINIO.md`
- **Documentation frontend** : `.github/documentation/FRONTEND_UPLOAD_IMAGES_REACT.md`
- **Instructions Copilot** : `.github/copilot-instructions.md` (section 🖼️)

---

**Quick Reference v1.0** - Dernière mise à jour : 15 février 2026

