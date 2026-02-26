import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { publiciteService } from "../services/publiciteService";
import type {
  CreatePubliciteDTO,
  PubliciteDTO,
  UpdatePubliciteDTO,
} from "../types";
import { createResourceHooks } from "./factories/createResourceHooks";
import {
  useImageUpload,
  type ImageFile,
  type ImageResponse,
} from "./useImageUpload";
import { formatFileName } from "@/common/utils/fileFormatter";

const publiciteHooks = createResourceHooks<
  PubliciteDTO,
  CreatePubliciteDTO,
  UpdatePubliciteDTO
>({
  resourceName: "publicites",
  service: publiciteService,
  customKeys: {
    actives: () => ["publicites", "actives"] as const,
  },
});

export const {
  keys: publiciteKeys,
  useList: usePublicites,
  useDetail: usePublicite,
  useUpdate: useUpdatePublicite,
  useDelete: useDeletePublicite,
} = publiciteHooks;

export const useCreatePublicite = () => {
  const queryClient = useQueryClient();
  const { uploadImages, progress, isUploading, hasErrors } = useImageUpload();

  const mutation = useMutation<PubliciteDTO, Error, CreatePubliciteDTO>({
    mutationFn: async (input) => {
      const payload = {
        ...input,
        listeImages: input.listeImages.map((img) => ({
          imageUuid: img.imageUuid,
          urlImage: formatFileName(img.urlImage),
          presignUrl: null,
          statut: null,
        })),
      };

      const publiciteCreee = await publiciteService.create(payload);

      const imagesFromBackend: Partial<ImageResponse>[] =
        (publiciteCreee.listeImages as Partial<ImageResponse>[]) ?? [];

      if (!imagesFromBackend || imagesFromBackend.length === 0) {
        return publiciteCreee;
      }

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
            presignUrl: f.presignUrl as string,
            id: f.imageUuid || "",
            name: backendFileName || "",
          };
        });

      if (filesForUpload.length > 0) {
        await uploadImages("publicites", publiciteCreee.uuid, filesForUpload);
      }

      return publiciteCreee;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: publiciteKeys.lists() });
    },
  });

  return {
    ...mutation,
    progress,
    isUploading,
    hasErrors,
  };
};

export const usePublicitesActives = () => {
  return useQuery<PubliciteDTO[], Error>({
    queryKey: publiciteKeys.actives(),
    queryFn: () => publiciteService.getActives(),
  });
};

export const useConfirmPubliciteImageUpload = () => {
  const queryClient = useQueryClient();

  return useMutation<
    void,
    Error,
    {
      publiciteUuid: string;
      imageUuid: string;
    }
  >({
    mutationFn: ({ publiciteUuid, imageUuid }) =>
      publiciteService.confirmImageUpload(publiciteUuid, imageUuid),
    onSuccess: (_, { publiciteUuid }) => {
      queryClient.invalidateQueries({
        queryKey: publiciteKeys.detail(publiciteUuid),
      });
    },
  });
};

export const useGetPubliciteImageUrl = (
  publiciteUuid: string,
  imageUuid: string,
) => {
  return useQuery<{ url: string }, Error>({
    queryKey: [...publiciteKeys.detail(publiciteUuid), "image-url", imageUuid],
    queryFn: () => publiciteService.getImageUrl(publiciteUuid, imageUuid),
    enabled: !!publiciteUuid && !!imageUuid,
  });
};
