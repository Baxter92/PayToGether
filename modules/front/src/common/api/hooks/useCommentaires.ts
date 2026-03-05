import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { commentaireService } from "../services/commentaireService";
import type {
  CommentaireDTO,
  CreateCommentaireDTO,
  UpdateCommentaireDTO,
} from "../types";
import { createResourceHooks } from "./factories/createResourceHooks";

const commentaireHooks = createResourceHooks<
  CommentaireDTO,
  CreateCommentaireDTO,
  UpdateCommentaireDTO
>({
  resourceName: "commentaires",
  service: commentaireService,
  customKeys: {
    byDeal: (dealUuid: string) => ["commentaires", "deal", dealUuid] as const,
    reponses: (commentaireParentUuid: string) =>
      ["commentaires", "reponses", commentaireParentUuid] as const,
  },
});

export const {
  keys: commentaireKeys,
  useList: useCommentaires,
  useDetail: useCommentaire,
  useCreate: useCreateCommentaire,
  useUpdate: useUpdateCommentaire,
  useDelete: useDeleteCommentaire,
} = commentaireHooks;

export const useCommentairesByDeal = (dealUuid: string) => {
  return useQuery<CommentaireDTO[], Error>({
    queryKey: commentaireKeys.byDeal(dealUuid),
    queryFn: () => commentaireService.getByDeal(dealUuid),
    enabled: !!dealUuid,
  });
};

export const useReponsesCommentaire = (commentaireParentUuid: string) => {
  return useQuery<CommentaireDTO[], Error>({
    queryKey: commentaireKeys.reponses(commentaireParentUuid),
    queryFn: () => commentaireService.getReponses(commentaireParentUuid),
    enabled: !!commentaireParentUuid,
  });
};

export const useUpdateCommentairePertinent = () => {
  const queryClient = useQueryClient();

  return useMutation<
    void,
    Error,
    { commentaireUuid: string; estPertinent: boolean }
  >({
    mutationFn: ({ commentaireUuid, estPertinent }) =>
      commentaireService.updatePertinent(commentaireUuid, estPertinent),
    onSuccess: (_, { commentaireUuid }) => {
      queryClient.invalidateQueries({
        queryKey: commentaireKeys.detail(commentaireUuid),
      });
      queryClient.invalidateQueries({
        queryKey: commentaireKeys.lists(),
      });
    },
  });
};

