import { apiClient } from "./apiClient";

export const imageService = {
  uploadToMinio: async (
    presignUrl: string,
    file: File,
    onProgress?: (progress: number) => void,
  ): Promise<void> => {
    console.log(presignUrl, "image service");

    try {
      await apiClient.put(presignUrl, {
        headers: {
          "Content-Type": file.type,
        },
        body: file,
        onUploadProgress: onProgress,
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
};
