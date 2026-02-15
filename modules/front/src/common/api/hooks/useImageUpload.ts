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
    images: ImageResponse[],
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
      images: ImageResponse[],
      files: ImageFile[],
    ) => {
      setIsUploading(true);
      setHasErrors(false);
      setProgress(new Map());

      try {
        // Créer une map pour associer fichiers et réponses backend
        // La clé est le nom du fichier original (sans le chemin du backend)
        const fileMap = new Map<string, File>();
        files.forEach((imageFile) => {
          fileMap.set(imageFile.file.name, imageFile.file);
        });

        console.log("📦 Files disponibles:", Array.from(fileMap.keys()));
        console.log("📥 Images à uploader:", images.map(img => ({
          uuid: img.uuid,
          urlImage: img.urlImage,
          nomUnique: img.nomUnique,
          presignUrl: img.presignUrl ? "✅" : "❌",
        })));

        // Uploader chaque image
        const uploadPromises = images.map(async (imageResponse) => {
          const imageId = imageResponse.uuid;

          // Le nomUnique contient le chemin complet: "deal/unique_00011.png"
          // Extraire juste le nom du fichier original avant le timestamp
          let originalFileName = imageResponse.nomUnique;

          // Si nomUnique contient un chemin (ex: deal/image.jpg_timestamp)
          if (originalFileName.includes("/")) {
            originalFileName = originalFileName.split("/").pop() || originalFileName;
          }
          // Retirer le timestamp si présent (format: nomfichier.ext_timestamp)
          const lastUnderscore = originalFileName.lastIndexOf("_");
          if (lastUnderscore > 0) {
            const extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            const baseName = originalFileName.substring(0, lastUnderscore);
            // Vérifier si ce qui suit le _ est un nombre (timestamp)
            const afterUnderscore = originalFileName.substring(lastUnderscore + 1);
            if (/^\d+/.test(afterUnderscore.replace(extension, ""))) {
              originalFileName = baseName + extension;
            }
          }

          const file = fileMap.get(originalFileName);

          if (!file) {
            console.error(`❌ Fichier non trouvé pour "${originalFileName}"`);
            console.error("   Fichiers disponibles:", Array.from(fileMap.keys()));
            updateProgress(imageId, {
              fileName: originalFileName,
              status: "error",
              error: "Fichier introuvable",
            });
            return;
          }

          console.log(`✅ Fichier trouvé: ${originalFileName} -> ${file.name}`);

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

            console.log(`🚀 Upload vers MinIO: ${imageResponse.nomUnique}`);
            console.log(`   URL présignée: ${imageResponse.presignUrl.substring(0, 100)}...`);

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

            console.log(`✅ Upload réussi: ${imageResponse.nomUnique}`);

            // Étape 3: Confirmation au backend
            updateProgress(imageId, {
              progress: 100,
              status: "confirming",
            });

            await imageService.confirmUpload(entityType, entityUuid, imageId);

            console.log(`✅ Confirmation réussie: ${imageResponse.nomUnique}`);

            // Étape 4: Succès
            updateProgress(imageId, {
              progress: 100,
              status: "success",
            });
          } catch (error) {
            console.error(`❌ Erreur upload ${file.name}:`, error);

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
