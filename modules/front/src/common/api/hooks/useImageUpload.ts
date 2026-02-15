import { useCallback, useState } from "react";
import { imageService } from "../services/imageService";

export interface ImageResponse {
  uuid: string;
  urlImage: string;
  nomUnique: string;
  presignUrl: string | null;
  statut: "PENDING" | "UPLOADED" | "FAILED";
  isPrincipal?: boolean;
  dateCreation: string;
  dateModification: string;
  file?: File;
}

export interface ImageFile {
  file: File;
  isPrincipal?: boolean;
  presignUrl: string;
  id: string;
  name: string;
}

export interface UploadProgress {
  imageId: string;
  fileName: string;
  progress: number;
  status: "validating" | "uploading" | "confirming" | "success" | "error";
  error?: string;
}

interface UseImageUploadReturn {
  uploadImages: (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    files: ImageFile[],
  ) => Promise<void>;
  progress: Map<string, UploadProgress>;
  isUploading: boolean;
  hasErrors: boolean;
}

export const useImageUpload = (): UseImageUploadReturn => {
  const [progress, setProgress] = useState<Map<string, UploadProgress>>(
    new Map(),
  );
  const [isUploading, setIsUploading] = useState(false);
  const [hasErrors, setHasErrors] = useState(false);

  const updateProgress = useCallback(
    (imageId: string, update: Partial<UploadProgress>) => {
      setProgress((prev) => {
        const newProgress = new Map(prev);
        const current = newProgress.get(imageId) || {
          imageId,
          fileName: "",
          progress: 0,
          status: "validating" as const,
        };
        newProgress.set(imageId, { ...current, ...update });
        return newProgress;
      });
    },
    [],
  );

  const uploadImages = useCallback(
    async (
      entityType: "deals" | "publicites" | "utilisateurs",
      entityUuid: string,
      files: ImageFile[],
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
        const uploadPromises = files.map(async (imageResponse) => {
          const imageId = imageResponse.id;
          const file = imageResponse.file;

          if (!file) {
            console.error(`Fichier non trouvé pour ${imageId}`);
            updateProgress(imageId, {
              fileName: imageResponse.name,
              status: "error",
              error: "Fichier introuvable",
            });
            return;
          }

          try {
            // Étape 1: Validation
            updateProgress(imageId, {
              fileName: file.name,
              progress: 0,
              status: "validating",
            });

            // Étape 2: Upload vers MinIO
            updateProgress(imageId, {
              progress: 0,
              status: "uploading",
            });

            if (!imageResponse.presignUrl) {
              throw new Error("Presign URL manquante");
            }
            await imageService.uploadToMinio(
              imageResponse.presignUrl,
              file,
              (progressPercent) => {
                updateProgress(imageId, {
                  progress: progressPercent,
                  status: "uploading",
                });
              },
            );

            // Étape 3: Confirmation au backend
            updateProgress(imageId, {
              progress: 100,
              status: "confirming",
            });

            imageService.confirmUpload(entityType, entityUuid, imageId);

            // Étape 4: Succès
            updateProgress(imageId, {
              progress: 100,
              status: "success",
            });
          } catch (error) {
            console.error(`Erreur upload ${file.name}:`, error);

            updateProgress(imageId, {
              status: "error",
              error: error instanceof Error ? error.message : "Erreur inconnue",
            });

            setHasErrors(true);
          }
        });

        await Promise.all(uploadPromises);
      } finally {
        setIsUploading(false);
      }
    },
    [updateProgress],
  );

  return {
    uploadImages,
    progress,
    isUploading,
    hasErrors,
  };
};
