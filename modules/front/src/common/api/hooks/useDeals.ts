import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { dealService } from "../services/dealService";
import type {
  CreateDealDTO,
  DealDTO,
  StatutDealType,
  UpdateDealDTO,
} from "../types";
import { apiClient } from "../services/apiClient";
import {
  useImageUpload,
  type ImageFile,
  type ImageResponse,
} from "./useImageUpload";
import { formatFileName } from "@/common/utils/fileFormatter";
import { createResourceHooks } from "./factories/createResourceHooks";

const dealHooks = createResourceHooks<DealDTO, CreateDealDTO, UpdateDealDTO>({
  resourceName: "deals",
  service: dealService,
  customKeys: {
    byStatut: (statut: StatutDealType) => ["deals", "statut", statut] as const,
    byCreateur: (createurUuid: string) =>
      ["deals", "createur", createurUuid] as const,
    byCategorie: (categorieUuid: string) =>
      ["deals", "categorie", categorieUuid] as const,
    villes: () => ["deals", "villes"] as const,
  },
});

export const {
  keys: dealKeys,
  useList: useDeals,
  useDetail: useDeal,
  useDelete: useDeleteDeal,
} = dealHooks;

export const useUpdateDeal = () => {
  const queryClient = useQueryClient();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<
    DealDTO,
    Error,
    { id: string; data: UpdateDealDTO }
  >({
    mutationFn: async ({ id, data }) => {
      // 1) Prepare payload
      const payload = {
        ...data,
        listeImages: data.listeImages?.map((img) => ({
          ...img,
          urlImage: formatFileName(
            img.nomUnique || img.file?.name || img.urlImage || "",
          ),
          nomUnique: formatFileName(img.nomUnique || img.file?.name || ""),
          statut: img.statut || null,
          isPrincipal: img.isPrincipal,
          presignUrl: null,
        })),
      };

      // 2) Call API
      const dealMisAJour = await dealService.update(id, payload as any);

      // 3) Handle image uploads if there are new ones (presigned URLs in response)
      const imagesFromBackend = dealMisAJour.listeImages ?? [];
      const filesForUpload: ImageFile[] = imagesFromBackend
        .filter((f) => f.presignUrl && f.statut === "PENDING")
        .map((f) => {
          // Find the corresponding File object from the input data
          const backendFileName = f.urlImage?.split("_")[0];
          const matchedImage = data.listeImages?.find(
            (img: any) =>
              formatFileName(img.file?.name || "")?.split(".")[0] ===
              backendFileName,
          );

          return {
            file: matchedImage?.file as File,
            isPrincipal: f.isPrincipal,
            presignUrl: f.presignUrl as string,
            id: f.imageUuid || "",
            name: backendFileName || "",
          };
        });

      if (filesForUpload.length > 0) {
        await uploadImages("deals", dealMisAJour.uuid, filesForUpload);
      }

      return dealMisAJour;
    },
    onSuccess: (updatedDeal) => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: dealKeys.detail(updatedDeal.uuid),
      });
      queryClient.invalidateQueries({ queryKey: dealKeys.byCreateur(updatedDeal.createurUuid) });
    },
  });

  return {
    ...mutation,
    progress,
    isUploading,
    hasErrors,
  };
};

// ===== QUERIES =====

export const useDealsByStatut = (statut: StatutDealType) => {
  return useQuery<DealDTO[], Error>({
    queryKey: dealKeys.byStatut(statut),
    queryFn: () => dealService.getByStatut(statut),
    enabled: !!statut,
  });
};

export const useDealsByCreateur = (createurUuid: string) => {
  return useQuery<DealDTO[], Error>({
    queryKey: dealKeys.byCreateur(createurUuid),
    queryFn: () => dealService.getByCreateur(createurUuid),
    enabled: !!createurUuid,
  });
};

export const useDealsByCategorie = (categorieUuid: string) => {
  return useQuery<DealDTO[], Error>({
    queryKey: dealKeys.byCategorie(categorieUuid),
    queryFn: () => dealService.getByCategorie(categorieUuid),
    enabled: !!categorieUuid,
  });
};

export const useDealVilles = () => {
  return useQuery<string[], Error>({
    queryKey: dealKeys.villes(),
    queryFn: () => dealService.getVilles(),
  });
};

// ===== MUTATIONS =====

export const useCreateDeal = () => {
  const queryClient = useQueryClient();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<DealDTO, Error, CreateDealDTO>({
    mutationFn: async (input) => {
      // 1) Construire payload pour la création
      // Le backend attend un tableau de noms de fichiers (listeImages: string[]).
      const payload = {
        ...input,
        listeImages: input.listeImages.map((img) => ({
          urlImage: formatFileName(img.nomUnique || ""),
          // Ensure nomUnique is formatted before sending to backend
          nomUnique: formatFileName(img.nomUnique || ""),
          statut: null,
          isPrincipal: img.isPrincipal,
          presignUrl: null,
        })),
      };

      // 2) Créer le deal côté backend (utilise ton dealService.create ou apiClient)
      // j'utilise apiClient.post pour rester aligné avec ta stack
      const dealCree = await apiClient.post<DealDTO>("/deals", {
        body: payload,
      });

      // 3) Si pas d'images à uploader, on peut retourner tout de suite
      const imagesFromBackend: Partial<ImageResponse>[] =
        dealCree.listeImages ?? [];
      if (!imagesFromBackend || imagesFromBackend.length === 0) {
        // invalide la liste pour forcer refresh
        return dealCree;
      }

      // 4) Préparer files (ImageFile[]) à passer au hook
      //    (le hook attend les files sous forme { file, preview, isPrincipal })
      const filesForUpload: ImageFile[] = imagesFromBackend
        .filter((f) => f.presignUrl && f.statut === "PENDING")
        .map((f) => {
          const backendFileName = f.urlImage?.split("_")[0];

          const matchedImage = input.listeImages.find(
            (img) =>
              formatFileName(img.file?.name || "")?.split(".")[0] ===
              backendFileName,
          );

          return {
            file: matchedImage?.file as File,
            isPrincipal: f.isPrincipal,
            presignUrl: f.presignUrl as string,
            id: f.imageUuid || "",
            name: backendFileName || "",
          };
        });

      // 5) Lancer les uploads + confirmations (uploadImages effectue la confirmation)
      //    uploadImages : (entityType, entityUuid, imagesFromBackend, filesForUpload)
      //    - entityType = "deals"
      //    - entityUuid = dealCree.uuid
      await uploadImages("deals", dealCree.uuid, filesForUpload);

      return dealCree;
    },

    onSuccess: (deal) => {
      // invalider listes / cache
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
       queryClient.invalidateQueries({ queryKey: dealKeys.byCreateur(deal.createurUuid) });
      // si tu as d'autres keys (detail), invalide-les aussi
    },

    onError: (err) => {
      console.error("Erreur création deal :", err);
    },
  });

  return {
    ...mutation,
    progress, // Map<string, UploadProgress> venant du hook useImageUpload
    isUploading, // bool (upload en cours)
    hasErrors, // bool (au moins une image en erreur)
  };
};

export const useUpdateDealStatus = () => {
  const queryClient = useQueryClient();

  return useMutation<
    DealDTO,
    Error,
    { id: string; statut: StatutDealType | string }
  >({
    mutationFn: ({ id, statut }) => dealService.updateStatus(id, statut),
    onSuccess: (updatedDeal) => {
    queryClient.invalidateQueries({ queryKey: dealKeys.byCreateur(updatedDeal.createurUuid) });
      queryClient.invalidateQueries({
        queryKey: dealKeys.detail(updatedDeal.uuid),
      });
    },
  });
};

export const useUpdateDealImages = () => {
  const queryClient = useQueryClient();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<DealDTO, Error, { id: string; images: any }>({
    mutationFn: async ({ id, images }) => {
      // 1) Construire payload pour la mise à jour des images
      const payload = {
        listeImages: images.map((img: any) => ({
          urlImage: formatFileName(img.nomUnique || img.file?.name || ""),
          nomUnique: formatFileName(img.nomUnique || img.file?.name || ""),
          statut: null,
          isPrincipal: img.isPrincipal,
          presignUrl: null,
        })),
      };

      // 2) Appeler l'API patch images
      const dealMisAJour = await dealService.updateImages(id, payload);

      // 3) Préparer files pour l'upload
      const imagesFromBackend: Partial<ImageResponse>[] =
        dealMisAJour.listeImages ?? [];
      if (!imagesFromBackend || imagesFromBackend.length === 0) {
        return dealMisAJour;
      }

      const filesForUpload: ImageFile[] = imagesFromBackend
        .filter((f) => f.presignUrl && f.statut === "PENDING")
        .map((f) => {
          const backendFileName = f.urlImage?.split("_")[0];
          const matchedImage = images.find(
            (img: any) =>
              formatFileName(img.file?.name || "")?.split(".")[0] ===
              backendFileName,
          );

          return {
            file: matchedImage?.file as File,
            isPrincipal: f.isPrincipal,
            presignUrl: f.presignUrl as string,
            id: f.imageUuid || "",
            name: backendFileName || "",
          };
        });

      // 4) Lancer les uploads
      if (filesForUpload.length > 0) {
        await uploadImages("deals", dealMisAJour.uuid, filesForUpload);
      }

      return dealMisAJour;
    },
    onSuccess: (updatedDeal) => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: dealKeys.detail(updatedDeal.uuid),
      });
    },
  });

  return {
    ...mutation,
    progress,
    isUploading,
    hasErrors,
  };
};

export const useConfirmDealImageUpload = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, { dealUuid: string; imageUuid: string }>({
    mutationFn: ({ dealUuid, imageUuid }) =>
      dealService.confirmImageUpload(dealUuid, imageUuid),
    onSuccess: (_, { dealUuid }) => {
      queryClient.invalidateQueries({ queryKey: dealKeys.detail(dealUuid) });
    },
  });
};

export const useGetDealImageUrl = (dealUuid: string, imageUuid?: string) => {
  return useQuery<{ url: string }, Error>({
    queryKey: [...dealKeys.detail(dealUuid), "image-url", imageUuid],
    queryFn: () => dealService.getImageUrl(dealUuid, imageUuid as any),
    enabled: !!dealUuid && !!imageUuid,
  });
};
