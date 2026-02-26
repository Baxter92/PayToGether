import { createResourceService } from "../module/service/resourceFactory";
import type {
  CreatePubliciteDTO,
  PubliciteDTO,
  UpdatePubliciteDTO,
} from "../types";
import { apiClient } from "./apiClient";

export const publiciteBaseService = createResourceService<PubliciteDTO>(
  apiClient,
  "/publicites",
);

export const publiciteService = {
  ...publiciteBaseService,

  create: (dto: CreatePubliciteDTO) =>
    apiClient.post<PubliciteDTO>("/publicites", { body: dto }),

  update: (id: string, dto: UpdatePubliciteDTO) =>
    apiClient.put<PubliciteDTO>(`/publicites/${id}`, { body: dto }),

  getActives: () => apiClient.get<PubliciteDTO[]>("/publicites/actives"),

  confirmImageUpload: (publiciteUuid: string, imageUuid: string) =>
    apiClient.patch<void>(
      `/publicites/${publiciteUuid}/images/${imageUuid}/confirm`,
    ),

  getImageUrl: (publiciteUuid: string, imageUuid: string) =>
    apiClient.get<{ url: string }>(
      `/publicites/${publiciteUuid}/images/${imageUuid}/url`,
    ),
};
