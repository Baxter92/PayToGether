# Guide Frontend - Upload d'images avec MinIO et React

## 📋 Table des matières
1. [Vue d'ensemble](#vue-densemble)
2. [Installation et Configuration](#installation-et-configuration)
3. [Concepts clés](#concepts-clés)
4. [Implémentation complète](#implémentation-complète)
5. [Composants React](#composants-react)
6. [Gestion des états](#gestion-des-états)
7. [Gestion des erreurs](#gestion-des-erreurs)
8. [Exemples pratiques](#exemples-pratiques)
9. [Bonnes pratiques](#bonnes-pratiques)
10. [Troubleshooting](#troubleshooting)

---

## Vue d'ensemble

Ce guide explique comment uploader des images depuis un frontend **React/TypeScript** vers **MinIO** en utilisant les **URL présignées** générées par le backend.

### Flux d'upload complet

```
┌─────────────────────────────────────────────────────────────────┐
│                    ÉTAPE 1 : Préparation                         │
│  Utilisateur sélectionne fichiers → Validation côté client      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              ÉTAPE 2 : Création de l'entité                      │
│  POST /api/deals avec métadonnées images                        │
│  Backend répond avec : uuid, presignUrl, nomUnique pour chaque  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                ÉTAPE 3 : Upload vers MinIO                       │
│  PUT {presignUrl} avec fichier binaire                          │
│  Upload direct sans passer par le backend                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              ÉTAPE 4 : Confirmation au backend                   │
│  PATCH /api/deals/{dealUuid}/images/{imageUuid}/confirm        │
│  Statut passe de PENDING à UPLOADED                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Installation et Configuration

### 1. Installation des dépendances

```bash
# Package MinIO pour JavaScript
npm install minio

# OU avec yarn
yarn add minio

# OU avec pnpm
pnpm add minio

# Packages supplémentaires recommandés
npm install axios react-dropzone
```

### 2. Structure du projet

```
src/
├── api/
│   ├── dealApi.ts              # Appels API pour les deals
│   ├── imageApi.ts             # Appels API pour les images
│   └── minioClient.ts          # Configuration client MinIO
├── components/
│   ├── ImageUploader.tsx       # Composant d'upload générique
│   ├── DealFormWithImages.tsx  # Formulaire deal avec images
│   └── ImagePreview.tsx        # Preview des images
├── hooks/
│   ├── useImageUpload.ts       # Hook custom pour upload
│   └── useDealCreation.ts      # Hook pour création deal
├── types/
│   └── image.types.ts          # Types TypeScript
└── utils/
    └── imageValidation.ts      # Validation des fichiers
```

---

## Concepts clés

### 1. Structure des données

#### Réponse backend après création d'entité

```typescript
interface DealResponseDTO {
  uuid: string;
  titre: string;
  description: string;
  prixDeal: number;
  listeImages: ImageResponseDTO[];
  // ...autres champs
}

interface ImageResponseDTO {
  uuid: string;              // ID unique de l'image en base
  urlImage: string;          // Nom original du fichier
  nomUnique: string;         // Nom avec timestamp (ex: "photo_1707394123456.jpg")
  presignUrl: string;        // URL présignée pour l'upload
  statut: 'PENDING' | 'UPLOADED' | 'FAILED';
  isPrincipal?: boolean;     // Pour les deals
  dateCreation: string;
  dateModification: string;
}
```

#### Données à envoyer pour créer une entité

```typescript
interface DealCreateDTO {
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  listeImages: ImageMetadataDTO[];  // Métadonnées uniquement, pas les fichiers
}

interface ImageMetadataDTO {
  urlImage: string;          // Nom du fichier original
  isPrincipal?: boolean;
  statut: 'PENDING';         // Toujours PENDING à la création
}
```

### 2. Cycle de vie d'un upload

```typescript
enum UploadStatus {
  IDLE = 'idle',               // Pas d'upload en cours
  VALIDATING = 'validating',   // Validation du fichier
  CREATING = 'creating',       // Création de l'entité backend
  UPLOADING = 'uploading',     // Upload vers MinIO
  CONFIRMING = 'confirming',   // Confirmation au backend
  SUCCESS = 'success',         // Upload réussi
  ERROR = 'error'              // Erreur
}
```

---

## Implémentation complète

### 1. Types TypeScript

```typescript
// src/types/image.types.ts

export interface ImageFile {
  file: File;
  preview: string;
  isPrincipal?: boolean;
}

export interface ImageMetadata {
  urlImage: string;
  isPrincipal?: boolean;
  statut: 'PENDING';
}

export interface ImageResponse {
  uuid: string;
  urlImage: string;
  nomUnique: string;
  presignUrl: string;
  statut: 'PENDING' | 'UPLOADED' | 'FAILED';
  isPrincipal?: boolean;
  dateCreation: string;
  dateModification: string;
}

export interface UploadProgress {
  imageId: string;
  fileName: string;
  progress: number;
  status: 'validating' | 'uploading' | 'confirming' | 'success' | 'error';
  error?: string;
}
```

### 2. API Client pour les images

```typescript
// src/api/imageApi.ts

import axios from 'axios';
import { ImageResponse } from '../types/image.types';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

export class ImageApi {
  /**
   * Upload un fichier vers MinIO en utilisant l'URL présignée
   */
  static async uploadToMinio(
    presignUrl: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<void> {
    try {
      await axios.put(presignUrl, file, {
        headers: {
          'Content-Type': file.type,
        },
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total) {
            const percentCompleted = Math.round(
              (progressEvent.loaded * 100) / progressEvent.total
            );
            onProgress?.(percentCompleted);
          }
        },
      });
    } catch (error) {
      console.error('Erreur upload MinIO:', error);
      throw new Error('Échec de l\'upload vers MinIO');
    }
  }

  /**
   * Confirme l'upload d'une image au backend (PENDING → UPLOADED)
   */
  static async confirmUpload(
    entityType: 'deals' | 'publicites' | 'utilisateurs',
    entityUuid: string,
    imageUuid: string
  ): Promise<void> {
    try {
      await axios.patch(
        `${API_BASE_URL}/api/${entityType}/${entityUuid}/images/${imageUuid}/confirm`
      );
    } catch (error) {
      console.error('Erreur confirmation upload:', error);
      throw new Error('Échec de la confirmation d\'upload');
    }
  }

  /**
   * Marque une image comme en erreur
   */
  static async markAsFailed(
    entityType: 'deals' | 'publicites' | 'utilisateurs',
    entityUuid: string,
    imageUuid: string,
    errorMessage: string
  ): Promise<void> {
    try {
      await axios.patch(
        `${API_BASE_URL}/api/${entityType}/${entityUuid}/images/${imageUuid}/fail`,
        { error: errorMessage }
      );
    } catch (error) {
      console.error('Erreur marquage échec:', error);
    }
  }

  /**
   * Obtient une URL de lecture pour une image uploadée
   */
  static async getImageReadUrl(
    entityType: 'deals' | 'publicites' | 'utilisateurs',
    entityUuid: string,
    imageUuid: string
  ): Promise<string> {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/api/${entityType}/${entityUuid}/images/${imageUuid}/url`
      );
      return response.data.url;
    } catch (error) {
      console.error('Erreur récupération URL lecture:', error);
      throw new Error('Impossible de récupérer l\'URL de l\'image');
    }
  }
}
```

### 3. API Client pour les deals

```typescript
// src/api/dealApi.ts

import axios from 'axios';
import { ImageMetadata } from '../types/image.types';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

export interface DealCreateRequest {
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  createurUuid: string;
  categorieUuid: string;
  ville: string;
  listeImages: ImageMetadata[];  // Métadonnées uniquement
}

export interface DealResponse {
  uuid: string;
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  statut: string;
  ville: string;
  listeImages: ImageResponse[];
  createurNom?: string;
  categorieNom?: string;
}

export class DealApi {
  /**
   * Crée un deal avec les métadonnées des images
   * Le backend génère les URL présignées
   */
  static async createDeal(dealData: DealCreateRequest): Promise<DealResponse> {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/api/deals`,
        dealData,
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      return response.data;
    } catch (error) {
      console.error('Erreur création deal:', error);
      throw new Error('Échec de la création du deal');
    }
  }

  /**
   * Récupère un deal avec ses images
   */
  static async getDeal(dealUuid: string): Promise<DealResponse> {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/api/deals/${dealUuid}`
      );
      return response.data;
    } catch (error) {
      console.error('Erreur récupération deal:', error);
      throw new Error('Deal introuvable');
    }
  }
}
```

### 4. Validation des fichiers

```typescript
// src/utils/imageValidation.ts

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

export class ImageValidator {
  // Configuration
  static readonly MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
  static readonly ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
  static readonly ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.webp', '.gif'];

  /**
   * Valide un fichier image
   */
  static validateFile(file: File): ValidationResult {
    const errors: string[] = [];

    // Vérification de la taille
    if (file.size > this.MAX_FILE_SIZE) {
      errors.push(
        `Le fichier "${file.name}" est trop volumineux (max 5 MB). Taille actuelle: ${(
          file.size /
          1024 /
          1024
        ).toFixed(2)} MB`
      );
    }

    // Vérification du type MIME
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      errors.push(
        `Le type de fichier "${file.type}" n'est pas autorisé. Types acceptés: JPG, PNG, WebP, GIF`
      );
    }

    // Vérification de l'extension
    const extension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
    if (!this.ALLOWED_EXTENSIONS.includes(extension)) {
      errors.push(
        `L'extension "${extension}" n'est pas autorisée. Extensions acceptées: ${this.ALLOWED_EXTENSIONS.join(
          ', '
        )}`
      );
    }

    // Vérification du nom de fichier
    if (file.name.length > 255) {
      errors.push('Le nom du fichier est trop long (max 255 caractères)');
    }

    return {
      isValid: errors.length === 0,
      errors,
    };
  }

  /**
   * Valide plusieurs fichiers
   */
  static validateFiles(files: File[]): ValidationResult {
    const allErrors: string[] = [];

    // Limite du nombre de fichiers
    const MAX_FILES = 10;
    if (files.length > MAX_FILES) {
      allErrors.push(`Vous ne pouvez uploader que ${MAX_FILES} images maximum`);
    }

    // Valider chaque fichier
    files.forEach((file) => {
      const result = this.validateFile(file);
      allErrors.push(...result.errors);
    });

    return {
      isValid: allErrors.length === 0,
      errors: allErrors,
    };
  }

  /**
   * Nettoie le nom de fichier (supprime caractères spéciaux)
   */
  static sanitizeFileName(fileName: string): string {
    // Séparer nom et extension
    const lastDotIndex = fileName.lastIndexOf('.');
    const name = fileName.substring(0, lastDotIndex);
    const extension = fileName.substring(lastDotIndex);

    // Nettoyer le nom (garder seulement alphanumériques, tirets et underscores)
    const cleanName = name
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '') // Supprimer accents
      .replace(/[^a-zA-Z0-9-_]/g, '_') // Remplacer caractères spéciaux
      .toLowerCase();

    return cleanName + extension.toLowerCase();
  }
}
```

---

## Composants React

### 1. Hook personnalisé pour l'upload d'images

```typescript
// src/hooks/useImageUpload.ts

import { useState, useCallback } from 'react';
import { ImageApi } from '../api/imageApi';
import { ImageFile, ImageResponse, UploadProgress } from '../types/image.types';

interface UseImageUploadReturn {
  uploadImages: (
    entityType: 'deals' | 'publicites' | 'utilisateurs',
    entityUuid: string,
    images: ImageResponse[],
    files: ImageFile[]
  ) => Promise<void>;
  progress: Map<string, UploadProgress>;
  isUploading: boolean;
  hasErrors: boolean;
}

export const useImageUpload = (): UseImageUploadReturn => {
  const [progress, setProgress] = useState<Map<string, UploadProgress>>(new Map());
  const [isUploading, setIsUploading] = useState(false);
  const [hasErrors, setHasErrors] = useState(false);

  const updateProgress = useCallback(
    (imageId: string, update: Partial<UploadProgress>) => {
      setProgress((prev) => {
        const newProgress = new Map(prev);
        const current = newProgress.get(imageId) || {
          imageId,
          fileName: '',
          progress: 0,
          status: 'validating' as const,
        };
        newProgress.set(imageId, { ...current, ...update });
        return newProgress;
      });
    },
    []
  );

  const uploadImages = useCallback(
    async (
      entityType: 'deals' | 'publicites' | 'utilisateurs',
      entityUuid: string,
      images: ImageResponse[],
      files: ImageFile[]
    ) => {
      setIsUploading(true);
      setHasErrors(false);
      setProgress(new Map());

      try {
        // Créer une map pour associer fichiers et réponses backend
        const fileMap = new Map<string, File>();
        files.forEach((imageFile) => {
          const cleanName = imageFile.file.name;
          fileMap.set(cleanName, imageFile.file);
        });

        // Uploader chaque image
        const uploadPromises = images.map(async (imageResponse) => {
          const imageId = imageResponse.uuid;
          
          // Extraire le nom original (avant le timestamp)
          const originalName = imageResponse.urlImage.split('_')[0] + 
                              imageResponse.urlImage.substring(
                                imageResponse.urlImage.lastIndexOf('.')
                              );
          
          const file = fileMap.get(originalName);

          if (!file) {
            console.error(`Fichier non trouvé pour ${originalName}`);
            updateProgress(imageId, {
              fileName: originalName,
              status: 'error',
              error: 'Fichier introuvable',
            });
            return;
          }

          try {
            // Étape 1: Validation
            updateProgress(imageId, {
              fileName: file.name,
              progress: 0,
              status: 'validating',
            });

            // Étape 2: Upload vers MinIO
            updateProgress(imageId, {
              progress: 0,
              status: 'uploading',
            });

            await ImageApi.uploadToMinio(
              imageResponse.presignUrl,
              file,
              (progressPercent) => {
                updateProgress(imageId, {
                  progress: progressPercent,
                  status: 'uploading',
                });
              }
            );

            // Étape 3: Confirmation au backend
            updateProgress(imageId, {
              progress: 100,
              status: 'confirming',
            });

            await ImageApi.confirmUpload(entityType, entityUuid, imageId);

            // Étape 4: Succès
            updateProgress(imageId, {
              progress: 100,
              status: 'success',
            });
          } catch (error) {
            console.error(`Erreur upload ${file.name}:`, error);
            
            updateProgress(imageId, {
              status: 'error',
              error: error instanceof Error ? error.message : 'Erreur inconnue',
            });

            // Marquer comme échoué côté backend
            await ImageApi.markAsFailed(
              entityType,
              entityUuid,
              imageId,
              error instanceof Error ? error.message : 'Upload failed'
            );

            setHasErrors(true);
          }
        });

        await Promise.all(uploadPromises);
      } finally {
        setIsUploading(false);
      }
    },
    [updateProgress]
  );

  return {
    uploadImages,
    progress,
    isUploading,
    hasErrors,
  };
};
```

### 2. Composant ImageUploader

```typescript
// src/components/ImageUploader.tsx

import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { ImageValidator } from '../utils/imageValidation';
import { ImageFile } from '../types/image.types';

interface ImageUploaderProps {
  maxImages?: number;
  onImagesSelected: (images: ImageFile[]) => void;
  existingImages?: ImageFile[];
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  maxImages = 10,
  onImagesSelected,
  existingImages = [],
}) => {
  const [selectedImages, setSelectedImages] = useState<ImageFile[]>(existingImages);
  const [errors, setErrors] = useState<string[]>([]);

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      setErrors([]);

      // Vérifier le nombre total d'images
      if (selectedImages.length + acceptedFiles.length > maxImages) {
        setErrors([`Vous ne pouvez sélectionner que ${maxImages} images maximum`]);
        return;
      }

      // Valider les fichiers
      const validation = ImageValidator.validateFiles(acceptedFiles);
      if (!validation.isValid) {
        setErrors(validation.errors);
        return;
      }

      // Créer les previews et nettoyer les noms
      const newImages: ImageFile[] = acceptedFiles.map((file) => ({
        file: new File(
          [file],
          ImageValidator.sanitizeFileName(file.name),
          { type: file.type }
        ),
        preview: URL.createObjectURL(file),
        isPrincipal: selectedImages.length === 0, // Première image = principale
      }));

      const updated = [...selectedImages, ...newImages];
      setSelectedImages(updated);
      onImagesSelected(updated);
    },
    [selectedImages, maxImages, onImagesSelected]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/jpeg': ['.jpg', '.jpeg'],
      'image/png': ['.png'],
      'image/webp': ['.webp'],
      'image/gif': ['.gif'],
    },
    maxSize: 5 * 1024 * 1024, // 5 MB
    multiple: true,
  });

  const removeImage = (index: number) => {
    const updated = selectedImages.filter((_, i) => i !== index);
    
    // Si on supprime l'image principale, définir la première comme principale
    if (selectedImages[index].isPrincipal && updated.length > 0) {
      updated[0].isPrincipal = true;
    }
    
    setSelectedImages(updated);
    onImagesSelected(updated);
    
    // Libérer l'URL de preview
    URL.revokeObjectURL(selectedImages[index].preview);
  };

  const setPrincipal = (index: number) => {
    const updated = selectedImages.map((img, i) => ({
      ...img,
      isPrincipal: i === index,
    }));
    setSelectedImages(updated);
    onImagesSelected(updated);
  };

  return (
    <div className="image-uploader">
      {/* Zone de drop */}
      <div
        {...getRootProps()}
        className={`dropzone ${isDragActive ? 'active' : ''}`}
        style={{
          border: '2px dashed #ccc',
          borderRadius: '8px',
          padding: '40px',
          textAlign: 'center',
          cursor: 'pointer',
          backgroundColor: isDragActive ? '#f0f0f0' : 'white',
        }}
      >
        <input {...getInputProps()} />
        <div>
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          <p>
            {isDragActive
              ? 'Déposez les images ici...'
              : 'Glissez-déposez des images ici, ou cliquez pour sélectionner'}
          </p>
          <p style={{ fontSize: '0.875rem', color: '#666' }}>
            JPG, PNG, WebP ou GIF (max 5 MB par image, {maxImages} images max)
          </p>
        </div>
      </div>

      {/* Erreurs de validation */}
      {errors.length > 0 && (
        <div className="errors" style={{ marginTop: '16px' }}>
          {errors.map((error, index) => (
            <div
              key={index}
              style={{
                padding: '12px',
                backgroundColor: '#fee',
                border: '1px solid #fcc',
                borderRadius: '4px',
                color: '#c00',
                marginBottom: '8px',
              }}
            >
              {error}
            </div>
          ))}
        </div>
      )}

      {/* Preview des images sélectionnées */}
      {selectedImages.length > 0 && (
        <div className="image-previews" style={{ marginTop: '24px' }}>
          <h4>Images sélectionnées ({selectedImages.length}/{maxImages})</h4>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))',
              gap: '16px',
              marginTop: '16px',
            }}
          >
            {selectedImages.map((image, index) => (
              <div
                key={index}
                style={{
                  position: 'relative',
                  border: image.isPrincipal ? '3px solid #007bff' : '1px solid #ddd',
                  borderRadius: '8px',
                  overflow: 'hidden',
                }}
              >
                <img
                  src={image.preview}
                  alt={`Preview ${index}`}
                  style={{
                    width: '100%',
                    height: '150px',
                    objectFit: 'cover',
                  }}
                />
                
                {/* Badge "Principale" */}
                {image.isPrincipal && (
                  <div
                    style={{
                      position: 'absolute',
                      top: '8px',
                      left: '8px',
                      backgroundColor: '#007bff',
                      color: 'white',
                      padding: '4px 8px',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      fontWeight: 'bold',
                    }}
                  >
                    Principale
                  </div>
                )}

                {/* Boutons d'action */}
                <div
                  style={{
                    position: 'absolute',
                    bottom: '8px',
                    right: '8px',
                    display: 'flex',
                    gap: '4px',
                  }}
                >
                  {!image.isPrincipal && (
                    <button
                      onClick={() => setPrincipal(index)}
                      style={{
                        padding: '4px 8px',
                        backgroundColor: 'white',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '0.75rem',
                      }}
                      title="Définir comme principale"
                    >
                      ⭐
                    </button>
                  )}
                  <button
                    onClick={() => removeImage(index)}
                    style={{
                      padding: '4px 8px',
                      backgroundColor: '#dc3545',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '0.75rem',
                    }}
                    title="Supprimer"
                  >
                    ✕
                  </button>
                </div>

                {/* Nom du fichier */}
                <div
                  style={{
                    padding: '8px',
                    backgroundColor: '#f8f9fa',
                    fontSize: '0.75rem',
                    textOverflow: 'ellipsis',
                    overflow: 'hidden',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {image.file.name}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
```

### 3. Composant de progression d'upload

```typescript
// src/components/UploadProgress.tsx

import React from 'react';
import { UploadProgress as UploadProgressType } from '../types/image.types';

interface UploadProgressProps {
  progress: Map<string, UploadProgressType>;
}

export const UploadProgressDisplay: React.FC<UploadProgressProps> = ({ progress }) => {
  if (progress.size === 0) return null;

  const progressArray = Array.from(progress.values());

  const getStatusText = (status: UploadProgressType['status']): string => {
    switch (status) {
      case 'validating':
        return 'Validation...';
      case 'uploading':
        return 'Upload en cours...';
      case 'confirming':
        return 'Confirmation...';
      case 'success':
        return 'Terminé ✓';
      case 'error':
        return 'Erreur ✗';
      default:
        return '';
    }
  };

  const getStatusColor = (status: UploadProgressType['status']): string => {
    switch (status) {
      case 'validating':
        return '#ffc107';
      case 'uploading':
        return '#007bff';
      case 'confirming':
        return '#17a2b8';
      case 'success':
        return '#28a745';
      case 'error':
        return '#dc3545';
      default:
        return '#6c757d';
    }
  };

  return (
    <div className="upload-progress" style={{ marginTop: '24px' }}>
      <h4>Progression de l'upload</h4>
      <div style={{ marginTop: '16px' }}>
        {progressArray.map((item) => (
          <div
            key={item.imageId}
            style={{
              marginBottom: '16px',
              padding: '16px',
              border: '1px solid #ddd',
              borderRadius: '8px',
              backgroundColor: '#f8f9fa',
            }}
          >
            {/* Nom du fichier */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '8px',
              }}
            >
              <span style={{ fontWeight: 'bold', fontSize: '0.875rem' }}>
                {item.fileName}
              </span>
              <span
                style={{
                  fontSize: '0.875rem',
                  color: getStatusColor(item.status),
                  fontWeight: 'bold',
                }}
              >
                {getStatusText(item.status)}
              </span>
            </div>

            {/* Barre de progression */}
            {item.status !== 'error' && (
              <div
                style={{
                  width: '100%',
                  height: '8px',
                  backgroundColor: '#e9ecef',
                  borderRadius: '4px',
                  overflow: 'hidden',
                }}
              >
                <div
                  style={{
                    width: `${item.progress}%`,
                    height: '100%',
                    backgroundColor: getStatusColor(item.status),
                    transition: 'width 0.3s ease',
                  }}
                />
              </div>
            )}

            {/* Pourcentage */}
            {item.status === 'uploading' && (
              <div
                style={{
                  marginTop: '4px',
                  fontSize: '0.75rem',
                  color: '#6c757d',
                  textAlign: 'right',
                }}
              >
                {item.progress}%
              </div>
            )}

            {/* Message d'erreur */}
            {item.error && (
              <div
                style={{
                  marginTop: '8px',
                  padding: '8px',
                  backgroundColor: '#f8d7da',
                  border: '1px solid #f5c6cb',
                  borderRadius: '4px',
                  color: '#721c24',
                  fontSize: '0.75rem',
                }}
              >
                {item.error}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Résumé global */}
      <div
        style={{
          marginTop: '16px',
          padding: '12px',
          backgroundColor: '#e7f3ff',
          border: '1px solid #b3d9ff',
          borderRadius: '4px',
        }}
      >
        <strong>Résumé:</strong>{' '}
        {progressArray.filter((p) => p.status === 'success').length} / {progressArray.length}{' '}
        images uploadées avec succès
      </div>
    </div>
  );
};
```

---

## Exemples pratiques

### Exemple complet : Créer un Deal avec images

```typescript
// src/pages/CreateDealPage.tsx

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ImageUploader } from '../components/ImageUploader';
import { UploadProgressDisplay } from '../components/UploadProgress';
import { useImageUpload } from '../hooks/useImageUpload';
import { DealApi } from '../api/dealApi';
import { ImageFile, ImageMetadata } from '../types/image.types';

export const CreateDealPage: React.FC = () => {
  const navigate = useNavigate();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const [formData, setFormData] = useState({
    titre: '',
    description: '',
    prixDeal: 0,
    prixPart: 0,
    nbParticipants: 1,
    dateDebut: '',
    dateFin: '',
    createurUuid: '', // À récupérer depuis le contexte utilisateur
    categorieUuid: '',
    ville: '',
  });

  const [selectedImages, setSelectedImages] = useState<ImageFile[]>([]);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsCreating(true);

    try {
      // ÉTAPE 1: Préparer les métadonnées des images
      const imageMetadata: ImageMetadata[] = selectedImages.map((img) => ({
        urlImage: img.file.name,
        isPrincipal: img.isPrincipal,
        statut: 'PENDING' as const,
      }));

      // ÉTAPE 2: Créer le deal avec les métadonnées
      console.log('Création du deal avec métadonnées images...');
      const dealResponse = await DealApi.createDeal({
        ...formData,
        listeImages: imageMetadata,
      });

      console.log('Deal créé:', dealResponse);
      console.log('Images avec URL présignées:', dealResponse.listeImages);

      // ÉTAPE 3: Uploader les images vers MinIO
      if (dealResponse.listeImages.length > 0) {
        console.log('Début upload des images...');
        await uploadImages(
          'deals',
          dealResponse.uuid,
          dealResponse.listeImages,
          selectedImages
        );
        console.log('Upload terminé');
      }

      // ÉTAPE 4: Redirection
      if (!hasErrors) {
        setTimeout(() => {
          navigate(`/deals/${dealResponse.uuid}`);
        }, 2000);
      }
    } catch (err) {
      console.error('Erreur création deal:', err);
      setError(err instanceof Error ? err.message : 'Une erreur est survenue');
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div className="create-deal-page" style={{ maxWidth: '800px', margin: '0 auto', padding: '24px' }}>
      <h1>Créer un nouveau Deal</h1>

      <form onSubmit={handleSubmit}>
        {/* Champs du formulaire */}
        <div style={{ marginBottom: '16px' }}>
          <label htmlFor="titre" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            Titre *
          </label>
          <input
            id="titre"
            type="text"
            value={formData.titre}
            onChange={(e) => setFormData({ ...formData, titre: e.target.value })}
            required
            style={{
              width: '100%',
              padding: '8px',
              border: '1px solid #ddd',
              borderRadius: '4px',
            }}
          />
        </div>

        <div style={{ marginBottom: '16px' }}>
          <label htmlFor="description" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            Description *
          </label>
          <textarea
            id="description"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            required
            rows={4}
            style={{
              width: '100%',
              padding: '8px',
              border: '1px solid #ddd',
              borderRadius: '4px',
            }}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
          <div>
            <label htmlFor="prixDeal" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              Prix total *
            </label>
            <input
              id="prixDeal"
              type="number"
              value={formData.prixDeal}
              onChange={(e) => setFormData({ ...formData, prixDeal: parseFloat(e.target.value) })}
              required
              min="0"
              step="0.01"
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
              }}
            />
          </div>

          <div>
            <label htmlFor="prixPart" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              Prix par part *
            </label>
            <input
              id="prixPart"
              type="number"
              value={formData.prixPart}
              onChange={(e) => setFormData({ ...formData, prixPart: parseFloat(e.target.value) })}
              required
              min="0"
              step="0.01"
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
              }}
            />
          </div>
        </div>

        {/* Upload d'images */}
        <div style={{ marginBottom: '24px' }}>
          <h3>Images du Deal</h3>
          <ImageUploader
            maxImages={10}
            onImagesSelected={setSelectedImages}
            existingImages={selectedImages}
          />
        </div>

        {/* Affichage des erreurs */}
        {error && (
          <div
            style={{
              marginBottom: '16px',
              padding: '12px',
              backgroundColor: '#f8d7da',
              border: '1px solid #f5c6cb',
              borderRadius: '4px',
              color: '#721c24',
            }}
          >
            {error}
          </div>
        )}

        {/* Progression de l'upload */}
        {(isUploading || progress.size > 0) && <UploadProgressDisplay progress={progress} />}

        {/* Boutons */}
        <div style={{ marginTop: '24px', display: 'flex', gap: '16px' }}>
          <button
            type="submit"
            disabled={isCreating || isUploading}
            style={{
              padding: '12px 24px',
              backgroundColor: isCreating || isUploading ? '#6c757d' : '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: isCreating || isUploading ? 'not-allowed' : 'pointer',
              fontWeight: 'bold',
            }}
          >
            {isCreating ? 'Création en cours...' : isUploading ? 'Upload en cours...' : 'Créer le Deal'}
          </button>

          <button
            type="button"
            onClick={() => navigate('/deals')}
            disabled={isCreating || isUploading}
            style={{
              padding: '12px 24px',
              backgroundColor: 'white',
              color: '#6c757d',
              border: '1px solid #6c757d',
              borderRadius: '4px',
              cursor: isCreating || isUploading ? 'not-allowed' : 'pointer',
            }}
          >
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};
```

---

## Gestion des états

### États possibles pendant le processus

```typescript
type DealCreationState =
  | { status: 'idle' }
  | { status: 'validating-form' }
  | { status: 'creating-deal' }
  | { status: 'uploading-images'; progress: Map<string, number> }
  | { status: 'confirming-uploads' }
  | { status: 'success'; dealUuid: string }
  | { status: 'error'; error: string };
```

### Machine à états complète

```typescript
// src/hooks/useDealCreationStateMachine.ts

import { useState } from 'react';

type State =
  | 'idle'
  | 'validating'
  | 'creating'
  | 'uploading'
  | 'confirming'
  | 'success'
  | 'error';

interface StateMachine {
  state: State;
  error: string | null;
  dealUuid: string | null;
  transition: (newState: State, data?: { error?: string; dealUuid?: string }) => void;
  reset: () => void;
}

export const useDealCreationStateMachine = (): StateMachine => {
  const [state, setState] = useState<State>('idle');
  const [error, setError] = useState<string | null>(null);
  const [dealUuid, setDealUuid] = useState<string | null>(null);

  const transition = (
    newState: State,
    data?: { error?: string; dealUuid?: string }
  ) => {
    console.log(`State transition: ${state} → ${newState}`);
    
    setState(newState);
    
    if (data?.error) {
      setError(data.error);
    }
    
    if (data?.dealUuid) {
      setDealUuid(data.dealUuid);
    }
  };

  const reset = () => {
    setState('idle');
    setError(null);
    setDealUuid(null);
  };

  return { state, error, dealUuid, transition, reset };
};
```

---

## Bonnes pratiques

### 1. Nettoyage des URL de preview

```typescript
// Toujours nettoyer les URL de preview pour éviter les fuites mémoire
useEffect(() => {
  return () => {
    selectedImages.forEach((image) => {
      URL.revokeObjectURL(image.preview);
    });
  };
}, [selectedImages]);
```

### 2. Retry automatique en cas d'échec

```typescript
const uploadWithRetry = async (
  presignUrl: string,
  file: File,
  maxRetries: number = 3
): Promise<void> => {
  let lastError: Error | null = null;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      console.log(`Tentative ${attempt}/${maxRetries} pour ${file.name}`);
      await ImageApi.uploadToMinio(presignUrl, file);
      return; // Succès
    } catch (error) {
      lastError = error instanceof Error ? error : new Error('Upload failed');
      console.error(`Échec tentative ${attempt}:`, lastError.message);
      
      if (attempt < maxRetries) {
        // Attendre avant de réessayer (backoff exponentiel)
        await new Promise((resolve) => setTimeout(resolve, 1000 * attempt));
      }
    }
  }

  throw lastError;
};
```

### 3. Upload en parallèle avec limite

```typescript
// Limiter le nombre d'uploads simultanés pour ne pas surcharger le réseau
const uploadImagesWithConcurrency = async (
  images: ImageResponse[],
  files: ImageFile[],
  maxConcurrent: number = 3
): Promise<void> => {
  const queue = [...images];
  const active: Promise<void>[] = [];

  while (queue.length > 0 || active.length > 0) {
    // Remplir jusqu'au max concurrent
    while (active.length < maxConcurrent && queue.length > 0) {
      const image = queue.shift()!;
      const file = files.find((f) => f.file.name === image.urlImage.split('_')[0]);
      
      if (file) {
        const promise = uploadSingleImage(image, file).finally(() => {
          const index = active.indexOf(promise);
          if (index > -1) active.splice(index, 1);
        });
        active.push(promise);
      }
    }

    // Attendre qu'au moins un upload se termine
    if (active.length > 0) {
      await Promise.race(active);
    }
  }
};
```

### 4. Variables d'environnement

```typescript
// .env.development
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_MINIO_MAX_FILE_SIZE=5242880
REACT_APP_MINIO_MAX_FILES=10

// .env.production
REACT_APP_API_BASE_URL=https://devbff.dealtogether.ca
REACT_APP_MINIO_MAX_FILE_SIZE=5242880
REACT_APP_MINIO_MAX_FILES=10
```

### 5. Logging et monitoring

```typescript
// src/utils/logger.ts
export class UploadLogger {
  static logUploadStart(fileName: string, fileSize: number) {
    console.log(`[UPLOAD START] ${fileName} (${(fileSize / 1024 / 1024).toFixed(2)} MB)`);
  }

  static logUploadProgress(fileName: string, progress: number) {
    console.log(`[UPLOAD PROGRESS] ${fileName}: ${progress}%`);
  }

  static logUploadSuccess(fileName: string, duration: number) {
    console.log(`[UPLOAD SUCCESS] ${fileName} in ${(duration / 1000).toFixed(2)}s`);
  }

  static logUploadError(fileName: string, error: Error) {
    console.error(`[UPLOAD ERROR] ${fileName}:`, error.message);
  }
}
```

---

## Troubleshooting

### Problème 1: CORS errors

**Symptôme**: `Access to fetch at 'https://minio:9000/...' from origin 'http://localhost:3000' has been blocked by CORS policy`

**Solution**: Configurer CORS sur MinIO

```bash
# Via MinIO CLI
mc alias set myminio http://localhost:9000 minioadmin minioadmin
mc admin policy set myminio cors-allow-all
```

### Problème 2: URL présignée expirée

**Symptôme**: Upload échoue avec erreur 403

**Solution**: Vérifier la durée de validité et uploader rapidement

```typescript
// Vérifier si l'URL est encore valide
const isPresignedUrlValid = (presignUrl: string): boolean => {
  try {
    const url = new URL(presignUrl);
    const expiresParam = url.searchParams.get('X-Amz-Expires');
    const dateParam = url.searchParams.get('X-Amz-Date');
    
    if (!expiresParam || !dateParam) return false;
    
    const expiresIn = parseInt(expiresParam, 10);
    const issueDate = new Date(dateParam);
    const expiryDate = new Date(issueDate.getTime() + expiresIn * 1000);
    
    return expiryDate > new Date();
  } catch {
    return false;
  }
};
```

### Problème 3: Images ne s'affichent pas

**Symptôme**: Images uploadées mais ne s'affichent pas

**Solution**: Demander une URL de lecture au backend

```typescript
// Ne pas utiliser directement l'URL d'upload pour l'affichage
// Demander une URL de lecture au backend
const imageReadUrl = await ImageApi.getImageReadUrl('deals', dealUuid, imageUuid);
setImageUrl(imageReadUrl);
```

### Problème 4: Fichiers trop volumineux

**Symptôme**: Upload échoue ou est très lent

**Solution**: Compresser les images côté client

```typescript
import imageCompression from 'browser-image-compression';

const compressImage = async (file: File): Promise<File> => {
  const options = {
    maxSizeMB: 1,
    maxWidthOrHeight: 1920,
    useWebWorker: true,
  };

  try {
    const compressedFile = await imageCompression(file, options);
    console.log(`Compressed ${file.name}: ${file.size} → ${compressedFile.size} bytes`);
    return compressedFile;
  } catch (error) {
    console.error('Compression failed:', error);
    return file; // Retourner le fichier original si échec
  }
};
```

---

## Résumé des étapes

### Workflow complet

```
1. Sélection des fichiers
   ├─ Validation côté client
   └─ Création des previews

2. Soumission du formulaire
   ├─ Préparation des métadonnées (nom, isPrincipal, statut: PENDING)
   └─ POST /api/deals avec métadonnées

3. Réception de la réponse
   ├─ Récupération des UUID, nomUnique, presignUrl
   └─ Association fichiers ↔ réponses backend

4. Upload vers MinIO
   ├─ PUT {presignUrl} pour chaque image
   ├─ Suivi de la progression
   └─ Gestion des erreurs/retry

5. Confirmation au backend
   ├─ PATCH /api/deals/{uuid}/images/{imageUuid}/confirm
   └─ Statut passe à UPLOADED

6. Affichage du résultat
   ├─ Succès → Redirection
   └─ Erreurs → Affichage des messages
```

### Points clés à retenir

✅ **Jamais envoyer les fichiers binaires au backend** (sauf pour de petits fichiers)  
✅ **Toujours valider côté client avant l'upload**  
✅ **Utiliser les URL présignées pour upload direct vers MinIO**  
✅ **Confirmer l'upload au backend après succès**  
✅ **Gérer les erreurs et permettre le retry**  
✅ **Nettoyer les URL de preview pour éviter les fuites mémoire**  
✅ **Logger les étapes pour faciliter le debugging**  

---

## Références

- 📚 [MinIO JavaScript Client](https://min.io/docs/minio/linux/developers/javascript/minio-javascript.html)
- 📚 [React Dropzone](https://react-dropzone.js.org/)
- 📚 [Axios Upload Progress](https://axios-http.com/docs/req_config)
- 📚 [Browser Image Compression](https://www.npmjs.com/package/browser-image-compression)

---

**Date de création** : 8 février 2026  
**Dernière mise à jour** : 8 février 2026  
**Auteur** : Équipe PayToGether
