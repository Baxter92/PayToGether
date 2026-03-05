import { createResourceService } from "../module/service/resourceFactory";
import type {
  CommentaireDTO,
  CreateCommentaireDTO,
  UpdateCommentaireDTO,
} from "../types";
import { apiClient } from "./apiClient";

export const commentaireBaseService = createResourceService<CommentaireDTO>(
  apiClient,
  "/commentaires",
);

export const commentaireService = {
  ...commentaireBaseService,

  create: (dto: CreateCommentaireDTO) =>
    apiClient.post<CommentaireDTO>("/commentaires", { body: dto }),

  update: (id: string, dto: UpdateCommentaireDTO) =>
    apiClient.put<CommentaireDTO>(`/commentaires/${id}`, { body: dto }),

  getByDeal: (dealUuid: string) =>
    apiClient.get<CommentaireDTO[]>(`/commentaires/deal/${dealUuid}`),

  getReponses: (commentaireParentUuid: string) =>
    apiClient.get<CommentaireDTO[]>(
      `/commentaires/${commentaireParentUuid}/reponses`,
    ),

  updatePertinent: (uuid: string, estPertinent: boolean) =>
    apiClient.patch<void>(`/commentaires/${uuid}/pertinent`, {
      body: { estPertinent },
    }),
};

