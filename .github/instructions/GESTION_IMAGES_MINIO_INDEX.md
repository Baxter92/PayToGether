# 📇 INDEX - Documentation Gestion Images MinIO

Index alphabétique de tous les concepts, fonctions et composants liés à la gestion des images avec MinIO.

---

## A

### ApiAdapter
- **Instruction complète** : Section "9. ApiAdapter - Délégation"
- **Copilot Instructions** : Pattern standard
- **Méthodes** : `mettreAJourStatutImage()`, `obtenirUrlLectureImage()`

### Architecture
- **Diagrammes** : Flux complet d'upload (21 étapes)
- **Instruction complète** : Section "Vue d'ensemble"
- **Copilot Instructions** : Section "Architecture complète"

---

## B

### Backend
- **Instruction complète** : Sections 1-10 (Backend)
- **Quick Reference** : Section "💻 Code Backend"
- **Checklist** : Quick Reference - Checklist Backend (6 points)

### Bucket MinIO
- **Configuration** : `minio.bucket.name` dans application.yml
- **Valeur** : `paytogether-images`

---

## C

### Checklist
- **Quick Reference** : Section "✅ Checklist rapide"
- **Instruction complète** : Section "✅ Checklist d'implémentation"

### Confirmation upload
- **Endpoint** : `PATCH /{entityUuid}/images/{imageUuid}/confirm`
- **Diagrammes** : Étape 13 du flux d'upload
- **Code** : Instruction complète, Section "8. Resource - Endpoints"

### Configuration
- **Fichier** : `application.yml`
- **Propriétés** :
  - `minio.endpoint`
  - `minio.bucket.name`
  - `minio.presigned.url.expiration`
- **Instruction complète** : Section "2. Configuration application.yml"

---

## D

### Dépendances Maven
```xml
io.minio:minio:8.5.7
commons-io:commons-io:2.15.1
```
- **Instruction complète** : Section "1. Dépendances Maven"

### Diagrammes
- **Fichier** : `GESTION_IMAGES_MINIO_DIAGRAMMES.md`
- **Contenu** : 3 diagrammes de séquence complets

---

## E

### Endpoints
1. `POST /api/deals` - Création avec images
2. `PATCH /{dealUuid}/images/{imageUuid}/confirm` - Confirmation
3. `GET /{dealUuid}/images/{imageUuid}/url` - URL de lecture

- **Instruction complète** : Section "8. Resource - Endpoints"
- **Quick Reference** : Section "Endpoints requis"

### Extension fichier
- **Extraction** : `FilenameUtils.getExtension(urlImage)`
- **Format final** : `.png`, `.jpg`, `.jpeg`

---

## F

### FAILED (statut)
- **Description** : Échec de l'upload
- **Transition** : PENDING → FAILED
- **Gestion** : Frontend (pas encore implémenté)

### FileManager
- **Localisation** : `bff-provider/src/main/java/.../utils/FileManager.java`
- **Méthodes** :
  - `generatePresignedUrl(folderName, uniqueFileName)` - Upload (PUT)
  - `generatePresignedUrlForRead(fullFileName)` - Lecture (GET)
  - `uploadMinioFile(...)` - Upload direct (non utilisé)
- **Instruction complète** : Section "3. FileManager"
- **Quick Reference** : Code complet

### FilenameUtils
- **Package** : `org.apache.commons.io.FilenameUtils`
- **Méthodes utilisées** :
  - `getBaseName(filename)` - Nom sans extension
  - `getExtension(filename)` - Extension seule
- **Utilisation** : Génération nom unique avec timestamp

### Flux
- **Upload** : 7 étapes (Quick Reference)
- **Upload détaillé** : 21 étapes (Diagrammes)
- **Lecture** : 11 étapes (Diagrammes)
- **Mise à jour** : 11 étapes (Diagrammes)

### Frontend
- **Instruction complète** : Sections "Implémentation Frontend"
- **Quick Reference** : Section "🎨 Code Frontend"
- **Technologies** : React, TypeScript, XMLHttpRequest

---

## G

### generatePresignedUrl()
- **Signature** : `String generatePresignedUrl(String folderName, String uniqueFileName)`
- **Méthode HTTP** : PUT
- **Utilisation** : Upload d'image
- **Expiration** : Configurée dans `minio.presigned.url.expiration`

### generatePresignedUrlForRead()
- **Signature** : `String generatePresignedUrlForRead(String fullFileName)`
- **Méthode HTTP** : GET
- **Utilisation** : Lecture/affichage d'image
- **Expiration** : Configurée dans `minio.presigned.url.expiration`

---

## H

### Hook useImageUpload
- **Fichier** : `src/common/api/hooks/useImageUpload.ts`
- **Fonctions** : `uploadImages()`, gestion progression
- **Instruction complète** : Section "2. Hook useImageUpload"
- **Quick Reference** : Code complet

### Hook useCreateDeal
- **Fichier** : `src/common/api/hooks/useCreateDeal.ts`
- **Utilise** : `useImageUpload()`
- **Instruction complète** : Section "3. Hook useCreateDeal"

---

## I

### imageService
- **Fichier** : `src/common/api/imageService.ts`
- **Méthodes** :
  - `uploadToMinio(presignUrl, file, onProgress)`
  - `confirmUpload(entityType, entityUuid, imageUuid)`
- **Instruction complète** : Section "1. Service d'upload d'images"
- **Quick Reference** : Code complet

### isPrincipal
- **Type** : `boolean`
- **Signification** : Image principale de l'entité
- **Règle** : Première image du tableau = `true`

---

## M

### mettreAJour()
- **Localisation** : ProviderAdapter
- **Logique** : Détecte changement URL → nouveau timestamp + PENDING
- **Méthode helper** : `mettreAJourImagesSiBesoin()`
- **Instruction complète** : Section "5. ProviderAdapter - Méthode mettreAJour()"

### mettreAJourImagesSiBesoin()
- **Localisation** : ProviderAdapter (méthode privée)
- **Logique** : Compare URL actuelle vs entrante
- **Action** : Si différent → nouveau nom + timestamp + PENDING
- **Code** : Instruction complète, Section "5"

### mettreAJourStatutImage()
- **Signature** : `void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut)`
- **Localisation** : Provider, Service, ApiAdapter
- **Utilisation** : Confirmation upload (PENDING → UPLOADED)
- **Instruction complète** : Section "6. ProviderAdapter - Méthodes de gestion"

### MinIO
- **Description** : Serveur de stockage d'objets (compatible S3)
- **Configuration** : application.yml
- **Client** : MinioClient (injecté dans FileManager)
- **Documentation** : https://min.io/docs

---

## N

### Nom de fichier
- **Format** : `{répertoire}/{baseName}_{timestamp}.{extension}`
- **Exemple** : `deals/unique_00011_1707988800000.png`
- **Composants** :
  - Répertoire : `Tools.DIRECTORY_XXX`
  - Base name : `FilenameUtils.getBaseName()`
  - Timestamp : `System.currentTimeMillis()`
  - Extension : `FilenameUtils.getExtension()`

### nomUnique
- **Type** : `String`
- **Valeur frontend** : `unique_00011.png`
- **Valeur backend (après traitement)** : `deals/unique_00011_1707988800000.png`
- **Utilisation** : Identifiant unique de l'image

---

## O

### obtenirUrlLectureImage()
- **Signature** : `String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid)`
- **Localisation** : Provider, Service, ApiAdapter
- **Utilisation** : Génération URL présignée pour lecture
- **Instruction complète** : Section "6. ProviderAdapter - Méthodes de gestion"

---

## P

### PENDING (statut)
- **Description** : En attente d'upload
- **État initial** : Toute nouvelle image
- **Transition** : PENDING → UPLOADED (après confirmation)
- **Génération presignUrl** : Oui (automatique)

### presignedUrlExpiry
- **Configuration** : `minio.presigned.url.expiration` (secondes)
- **Valeur par défaut** : 3600 (1 heure)
- **Utilisation** : Durée de validité des URL présignées

### presignUrl
- **Type** : `String`
- **Valeur** : URL présignée MinIO (longue URL avec signature)
- **Génération** : Automatique pour images PENDING
- **Utilisation frontend** : Upload direct vers MinIO

### Provider
- **Interface** : `bff-core/provider/`
- **Implémentation** : `bff-provider/adapter/` (suffix `ProviderAdapter`)
- **Méthodes images** :
  - `mettreAJourStatutImage()`
  - `obtenirUrlLectureImage()`
- **Instruction complète** : Section "7. Provider - Interface"

### ProviderAdapter
- **Localisation** : `bff-provider/adapter/`
- **Méthodes clés** :
  - `sauvegarder()`
  - `mettreAJour()`
  - `setPresignUrl()`
  - `mettreAJourImagesSiBesoin()`
  - `mettreAJourStatutImage()`
  - `obtenirUrlLectureImage()`
- **Instruction complète** : Sections 4-6

---

## Q

### Quick Reference
- **Fichier** : `GESTION_IMAGES_MINIO_QUICK_REF.md`
- **Utilisation** : Aide-mémoire pendant développement
- **Contenu** : Code prêt à copier-coller

---

## R

### Répertoires
- **Deals** : `Tools.DIRECTORY_DEALS_IMAGES` = `"deals/"`
- **Publicités** : `Tools.DIRECTORY_PUBLICITES_IMAGES` = `"publicites/"`
- **Utilisateurs** : `Tools.DIRECTORY_UTILISATEUR_IMAGES` = `"utilisateurs/"`

### Resource
- **Localisation** : `bff-api/resource/`
- **Endpoints images** : POST, PATCH `/confirm`, GET `/url`
- **Instruction complète** : Section "8. Resource - Endpoints"

---

## S

### sauvegarder()
- **Localisation** : ProviderAdapter
- **Logique** :
  1. Génération nom unique avec timestamp
  2. Sauvegarde en base (statut PENDING)
  3. Génération presignUrl
- **Instruction complète** : Section "4. ProviderAdapter - Méthode sauvegarder()"
- **Quick Reference** : Code complet

### Service
- **Interface** : `bff-core/domaine/service/`
- **Implémentation** : `bff-core/domaine/impl/` (suffix `ServiceImpl`)
- **Rôle** : Délégation vers Provider
- **Instruction complète** : Section "10. Service - Délégation"

### setPresignUrl()
- **Localisation** : ProviderAdapter (méthode privée)
- **Logique** : Filtre images PENDING → génère presignUrl
- **Appel** : Après `sauvegarder()` et `mettreAJour()`
- **Code** : Instruction complète, Section "4"

### StatutImage
- **Type** : `enum`
- **Valeurs** : `PENDING`, `UPLOADED`, `FAILED`
- **Localisation** : `bff-core/enumeration/StatutImage.java`
- **Documentation** : Toutes les instructions

### System.currentTimeMillis()
- **Utilisation** : Génération timestamp unique
- **Format** : Long (ex: 1707988800000)
- **Localisation** : Ajouté au nom de fichier

---

## T

### Timestamp
- **Génération** : `System.currentTimeMillis()`
- **Utilisation** : Garantit unicité du nom de fichier
- **Format** : Millisecondes depuis epoch
- **Exemple** : `1707988800000`

### Tools.java
- **Localisation** : `bff-provider/utils/Tools.java`
- **Constantes** :
  - `DIRECTORY_DEALS_IMAGES`
  - `DIRECTORY_PUBLICITES_IMAGES`
  - `DIRECTORY_UTILISATEUR_IMAGES`
- **Utilisation** : Toujours référencer ces constantes (jamais de chaîne en dur)

---

## U

### UPLOADED (statut)
- **Description** : Uploadé avec succès
- **Transition** : PENDING → UPLOADED (après confirmation)
- **Génération presignUrl** : Oui (pour lecture)

### uploadToMinio()
- **Signature** : `async uploadToMinio(presignUrl, file, onProgress)`
- **Méthode HTTP** : PUT
- **Localisation** : `imageService.ts`
- **Instruction complète** : Section "1. Service d'upload"

### URL présignée
- **Description** : URL temporaire avec signature cryptographique
- **Validité** : Configurée (1 heure par défaut)
- **Types** : PUT (upload) et GET (lecture)
- **Sécurité** : Pas de proxy backend, accès direct MinIO

### urlImage
- **Type** : `String`
- **Valeur frontend** : Nom original du fichier (ex: `image.jpg`)
- **Valeur backend** : Nom complet avec répertoire et timestamp

---

## X

### XMLHttpRequest
- **Utilisation** : Upload direct vers MinIO (méthode PUT)
- **Avantage** : Suivi de progression
- **Code** : `imageService.uploadToMinio()`

---

## 📚 Références principales

1. **Guide complet** : `GESTION_IMAGES_MINIO_GUIDE_COMPLET.md` (point d'entrée)
2. **Instruction** : `GESTION_IMAGES_MINIO_INSTRUCTION.md` (référence exhaustive)
3. **Quick Reference** : `GESTION_IMAGES_MINIO_QUICK_REF.md` (aide-mémoire)
4. **Diagrammes** : `GESTION_IMAGES_MINIO_DIAGRAMMES.md` (visualisation)
5. **Copilot** : `.github/copilot-instructions.md` (section 🖼️)

---

**Index v1.0** - Dernière mise à jour : 15 février 2026

