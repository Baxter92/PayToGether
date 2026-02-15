import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { dealService } from "../services/dealService";
import type {
  CreateDealDTO,
  DealDTO,
  StatutDeal,
  UpdateDealDTO,
} from "../types";
import { apiClient } from "../services/apiClient";
import {
  useImageUpload,
  type ImageFile,
  type ImageResponse,
} from "./useImageUpload";
import { createResourceHooks } from "./factories/createResourceHooks";

const dealHooks = createResourceHooks<DealDTO, CreateDealDTO, UpdateDealDTO>({
  resourceName: "deals",
  service: dealService,
  customKeys: {
    byStatut: (statut: typeof StatutDeal) =>
      ["deals", "statut", statut] as const,
    byCreateur: (createurUuid: string) =>
      ["deals", "createur", createurUuid] as const,
    byCategorie: (categorieUuid: string) =>
      ["deals", "categorie", categorieUuid] as const,
  },
});

export const {
  keys: dealKeys,
  useList: useDeals,
  useDetail: useDeal,
  useUpdate: useUpdateDeal,
  useDelete: useDeleteDeal,
} = dealHooks;

// ===== QUERIES =====

export const useDealsByStatut = (statut: typeof StatutDeal) => {
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

// ===== MUTATIONS =====

export const useCreateDeal = () => {
  const queryClient = useQueryClient();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<DealDTO, Error, CreateDealDTO>({
    mutationFn: async (input) => {
      console.log("🎯 Création du deal avec", input.listeImages?.length, "images");

      // 1) Construire payload pour la création
      const payload = {
        ...input,
        listeImages: input.listeImages?.map((img) => ({
          urlImage: img.urlImage,
          nomUnique: img.nomUnique,
          statut: null,
          isPrincipal: img.isPrincipal,
          presignUrl: null,
        })),
      };

      // 2) Créer le deal côté backend
      const dealCree = await apiClient.post<DealDTO>("/deals", {
        body: payload,
      });

      console.log("✅ Deal créé:", dealCree.uuid);
      console.log("📸 Images reçues du backend:", dealCree.listeImages);

      // 3) Si pas d'images à uploader, retourner
      const imagesFromBackend = dealCree.listeImages ?? [];
      if (imagesFromBackend.length === 0) {
        return dealCree;
      }

      // 4) Créer une map des fichiers par nom original
      const filesByName = new Map<string, File>();
      (input.listeImages || []).forEach((img) => {
        if (img.file) {
          filesByName.set(img.file.name, img.file);
        }
      });

      console.log("📁 Fichiers disponibles:", Array.from(filesByName.keys()));

      // 5) Préparer les fichiers pour upload
      const filesForUpload: ImageFile[] = [];

      imagesFromBackend.forEach((imageFromBackend) => {
        if (!imageFromBackend.presignUrl || imageFromBackend.statut !== "PENDING") {
          console.warn(`⚠️ Image ${imageFromBackend.nomUnique} ignorée (pas de presignUrl ou statut différent de PENDING)`);
          return;
        }

        // Trouver le fichier correspondant
        // Le nomUnique peut être "deal/image.jpg_1234567890"
        // On doit retrouver le fichier original "image.jpg"
        let matchedFile: File | undefined;

        for (const [fileName, file] of filesByName.entries()) {
          // Vérifier si le nomUnique contient le nom du fichier
          if (imageFromBackend.nomUnique?.includes(fileName) ||
              imageFromBackend.urlImage?.includes(fileName)) {
            matchedFile = file;
            break;
          }
        }

        if (matchedFile) {
          filesForUpload.push({
            file: matchedFile,
            isPrincipal: imageFromBackend.isPrincipal,
            presignUrl: imageFromBackend.presignUrl,
          });
          console.log(`✅ Fichier associé: ${matchedFile.name} -> ${imageFromBackend.nomUnique}`);
        } else {
          console.error(`❌ Aucun fichier trouvé pour: ${imageFromBackend.nomUnique}`);
        }
      });

      if (filesForUpload.length === 0) {
        console.warn("⚠️ Aucun fichier à uploader");
        return dealCree;
      }

      console.log(`🚀 Démarrage de l'upload de ${filesForUpload.length} fichier(s)`);

      // 6) Lancer les uploads + confirmations
      await uploadImages(
        "deals",
        dealCree.uuid,
        imagesFromBackend as ImageResponse[],
        filesForUpload,
      );

      // 7) Récupérer la version finale du deal
      const dealFinal = await apiClient.get<DealDTO>(`/deals/${dealCree.uuid}`);

      console.log("✅ Deal finalisé:", dealFinal.uuid);

      return dealFinal ?? dealCree;
    },

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
    },

    onError: (err) => {
      console.error("❌ Erreur création deal :", err);
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

export const useGetDealImageUrl = (dealUuid: string, imageUuid: string) => {
  return useQuery<{ url: string }, Error>({
    queryKey: [...dealKeys.detail(dealUuid), "image-url", imageUuid],
    queryFn: () => dealService.getImageUrl(dealUuid, imageUuid),
    enabled: !!dealUuid && !!imageUuid,
  });
};
