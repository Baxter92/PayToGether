import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { dealService } from "../services/dealService";
import type { Deal, StatutDeal } from "../types";
import { apiClient } from "../services/apiClient";
import {
  useImageUpload,
  type ImageFile,
  type ImageResponse,
} from "./useImageUpload";

// Clés de cache pour les requêtes
export const dealKeys = {
  all: ["deals"] as const,
  lists: () => [...dealKeys.all, "list"] as const,
  list: (filters: string) => [...dealKeys.lists(), { filters }] as const,
  details: () => [...dealKeys.all, "detail"] as const,
  detail: (id: string) => [...dealKeys.details(), id] as const,
  byStatut: (statut: typeof StatutDeal) =>
    [...dealKeys.all, "statut", statut] as const,
  byCreateur: (createurUuid: string) =>
    [...dealKeys.all, "createur", createurUuid] as const,
  byCategorie: (categorieUuid: string) =>
    [...dealKeys.all, "categorie", categorieUuid] as const,
};

// ===== QUERIES =====

export const useDeals = () => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.lists(),
    queryFn: () => dealService.list(),
  });
};

export const useDeal = (uuid: string) => {
  return useQuery<Deal, Error>({
    queryKey: dealKeys.detail(uuid),
    queryFn: () => dealService.getById(uuid),
    enabled: !!uuid,
  });
};

export const useDealsByStatut = (statut: typeof StatutDeal) => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.byStatut(statut),
    queryFn: () => dealService.getByStatut(statut),
    enabled: !!statut,
  });
};

export const useDealsByCreateur = (createurUuid: string) => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.byCreateur(createurUuid),
    queryFn: () => dealService.getByCreateur(createurUuid),
    enabled: !!createurUuid,
  });
};

export const useDealsByCategorie = (categorieUuid: string) => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.byCategorie(categorieUuid),
    queryFn: () => dealService.getByCategorie(categorieUuid),
    enabled: !!categorieUuid,
  });
};

// ===== MUTATIONS =====

export const useCreateDeal = () => {
  const queryClient = useQueryClient();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<
    Deal,
    Error,
    Omit<Deal, "uuid" | "dateCreation" | "dateModification">
  >({
    mutationFn: async (input) => {
      // 1) Construire payload pour la création (backend doit accepter listeImages)
      const payload = {
        ...input,
        listeImages: input.listeImages.map((img) => img.nomUnique),
        // ajoute autres champs si besoin
      };

      // 2) Créer le deal côté backend (utilise ton dealService.create ou apiClient)
      // j'utilise apiClient.post pour rester aligné avec ta stack
      const dealCree = await apiClient.post<Deal>("/deals", {
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
        .map((f) => ({
          file: input.listeImages.find((img) => img.nomUnique === f.nomUnique)
            ?.file as File,
          isPrincipal: f.isPrincipal,
          presignUrl: f.presignUrl as string,
        }));

      // 5) Lancer les uploads + confirmations (uploadImages effectue la confirmation)
      //    uploadImages : (entityType, entityUuid, imagesFromBackend, filesForUpload)
      //    - entityType = "deals"
      //    - entityUuid = dealCree.uuid
      await uploadImages(
        "deals",
        dealCree.uuid,
        imagesFromBackend as ImageResponse[],
        filesForUpload,
      );

      // 6) Optionnel : récupérer la version finale du deal (avec statuts d'images à jour)
      // Remplace par ton service si tu as dealService.getByUuid
      const dealFinal = await apiClient.get<any>(`/deals/${dealCree.uuid}`);

      return dealFinal ?? dealCree;
    },

    onSuccess: () => {
      // invalider listes / cache
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
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

export const useUpdateDeal = () => {
  const queryClient = useQueryClient();

  return useMutation<Deal, Error, { uuid: string; data: Partial<Deal> }>({
    mutationFn: ({ uuid, data }) => dealService.update(uuid, data),
    onSuccess: (updatedDeal) => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: dealKeys.detail(updatedDeal.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: dealKeys.byStatut(updatedDeal.statut),
      });
      queryClient.invalidateQueries({
        queryKey: dealKeys.byCreateur(updatedDeal.createurUuid),
      });
      queryClient.invalidateQueries({
        queryKey: dealKeys.byCategorie(updatedDeal.categorieUuid),
      });
    },
  });
};

export const useDeleteDeal = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => dealService.remove(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
    },
  });
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

export const useGetDealImageUrl = (dealUuid: string, imageUuid: string) => {
  return useQuery<{ url: string }, Error>({
    queryKey: [...dealKeys.detail(dealUuid), "image-url", imageUuid],
    queryFn: () => dealService.getImageUrl(dealUuid, imageUuid),
    enabled: !!dealUuid && !!imageUuid,
  });
};
