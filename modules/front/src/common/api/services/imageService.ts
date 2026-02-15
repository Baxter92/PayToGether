import { apiClient } from "./apiClient";

export const imageService = {
  /**
   * Upload direct vers MinIO via URL présignée
   * Utilise fetch natif pour éviter les intercepteurs d'apiClient
   */
  uploadToMinio: async (
    presignUrl: string,
    file: File,
    onProgress?: (progress: number) => void,
  ): Promise<void> => {
    console.log(presignUrl, "image service");

    try {
      // Utiliser XMLHttpRequest pour suivre la progression
      return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();

        // Suivi de la progression
        if (onProgress) {
          xhr.upload.addEventListener("progress", (event) => {
            if (event.lengthComputable) {
              const percentComplete = Math.round(
                (event.loaded / event.total) * 100,
              );
              onProgress(percentComplete);
            }
          });
        }

        // Gestion de la fin
        xhr.addEventListener("load", () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve();
          } else {
            reject(new Error(`Erreur HTTP ${xhr.status}: ${xhr.statusText}`));
          }
        });

        // Gestion des erreurs
        xhr.addEventListener("error", () => {
          reject(new Error("Erreur réseau lors de l'upload"));
        });

        xhr.addEventListener("abort", () => {
          reject(new Error("Upload annulé"));
        });

        // Configuration et envoi
        xhr.open("PUT", presignUrl);
        xhr.setRequestHeader("Content-Type", file.type);
        xhr.send(file);
      });
    } catch (error) {
      console.error("Erreur upload MinIO:", error);
      throw new Error("Échec de l'upload vers MinIO");
    }
  },

  confirmUpload: async (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    imageUuid: string,
  ): Promise<void> => {
    try {
      await apiClient.patch(
        `${entityType}/${entityUuid}/images/${imageUuid}/confirm`,
      );
    } catch (error) {
      console.error("Erreur confirmation upload:", error);
      throw new Error("Échec de la confirmation d'upload");
    }
  },
  confirmAllUploads: async (
    entityType: "deals" | "publicites" | "utilisateurs",
    entityUuid: string,
    imageUuids: string[],
  ): Promise<void> => {
    try {
      await apiClient.patch(
        `${entityType}/${entityUuid}/images/confirm-batch`,
        {
          body: imageUuids,
        },
      );
    } catch (error) {
      console.error("Erreur confirmation upload:", error);
      throw new Error("Échec de la confirmation d'upload");
    }
  },
};
