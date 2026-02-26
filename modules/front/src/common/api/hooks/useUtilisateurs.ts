import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { utilisateurService } from "../services/utilisateurService";
import type {
  CreateUtilisateurDTO,
  UtilisateurDTO,
  ReinitialiserMotDePasseDTO,
  ActiverUtilisateurDTO,
  AssignerRoleDTO,
} from "../types";
import { createResourceHooks } from "./factories/createResourceHooks";

const utilisateurHooks = createResourceHooks<
  UtilisateurDTO,
  CreateUtilisateurDTO,
  Partial<UtilisateurDTO>
>({
  resourceName: "utilisateurs",
  service: utilisateurService,
  customKeys: {
    byEmail: (email: string) => ["utilisateurs", "email", email] as const,
    exists: (email: string) => ["utilisateurs", "exists", email] as const,
  },
});

export const {
  keys: userKeys,
  useList: useUsers,
  useDetail: useUser,
  useCreate: useCreateUser,
  useUpdate: useUpdateUser,
  useDelete: useDeleteUser,
} = utilisateurHooks;

export const useUserByEmail = (email: string) => {
  return useQuery<UtilisateurDTO, Error>({
    queryKey: userKeys.byEmail(email),
    queryFn: () => utilisateurService.getByEmail(email),
    enabled: !!email,
  });
};

export const useUserExists = (email: string) => {
  return useQuery<boolean, Error>({
    queryKey: userKeys.exists(email),
    queryFn: () => utilisateurService.existsByEmail(email),
    enabled: !!email,
  });
};

export const useConfirmUserPhotoUpload = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (utilisateurUuid) =>
      utilisateurService.confirmPhotoUpload(utilisateurUuid),
    onSuccess: (_, utilisateurUuid) => {
      queryClient.invalidateQueries({
        queryKey: userKeys.detail(utilisateurUuid),
      });
    },
  });
};

export const useGetUserPhotoUrl = (utilisateurUuid: string) => {
  return useQuery<{ url: string }, Error>({
    queryKey: [...userKeys.detail(utilisateurUuid), "photo-url"],
    queryFn: () => utilisateurService.getPhotoUrl(utilisateurUuid),
    enabled: !!utilisateurUuid,
  });
};

export const useResetUserPassword = () => {
  return useMutation<void, Error, { utilisateurUuid: string; data: ReinitialiserMotDePasseDTO }>({
    mutationFn: ({ utilisateurUuid, data }) =>
      utilisateurService.resetPassword(utilisateurUuid, data),
  });
};

export const useSetUserEnabled = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, { utilisateurUuid: string; data: ActiverUtilisateurDTO }>({
    mutationFn: ({ utilisateurUuid, data }) =>
      utilisateurService.setEnabled(utilisateurUuid, data),
    onSuccess: (_, { utilisateurUuid }) => {
      queryClient.invalidateQueries({
        queryKey: userKeys.detail(utilisateurUuid),
      });
      queryClient.invalidateQueries({
        queryKey: userKeys.lists(),
      });
    },
  });
};

export const useAssignUserRole = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, { utilisateurUuid: string; data: AssignerRoleDTO }>({
    mutationFn: ({ utilisateurUuid, data }) =>
      utilisateurService.assignRole(utilisateurUuid, data),
    onSuccess: (_, { utilisateurUuid }) => {
      queryClient.invalidateQueries({
        queryKey: userKeys.detail(utilisateurUuid),
      });
      queryClient.invalidateQueries({
        queryKey: userKeys.lists(),
      });
    },
  });
};
