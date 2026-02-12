import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { utilisateurService } from "../services/utilisateurService";
import type { Utilisateur, CreerUtilisateurDTO } from "../types";

// Clés de cache pour les requêtes
export const userKeys = {
  all: ["utilisateurs"] as const,
  lists: () => [...userKeys.all, "list"] as const,
  list: (filters: string) => [...userKeys.lists(), { filters }] as const,
  details: () => [...userKeys.all, "detail"] as const,
  detail: (id: string) => [...userKeys.details(), id] as const,
  byEmail: (email: string) => [...userKeys.all, "email", email] as const,
  exists: (email: string) => [...userKeys.all, "exists", email] as const,
};

// ===== QUERIES =====

export const useUsers = () => {
  return useQuery<Utilisateur[], Error>({
    queryKey: userKeys.lists(),
    queryFn: () => utilisateurService.list(),
  });
};

export const useUser = (uuid: string) => {
  return useQuery<Utilisateur, Error>({
    queryKey: userKeys.detail(uuid),
    queryFn: () => utilisateurService.getById(uuid),
    enabled: !!uuid,
  });
};

export const useUserByEmail = (email: string) => {
  return useQuery<Utilisateur, Error>({
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

// ===== MUTATIONS =====

export const useCreateUser = () => {
  const queryClient = useQueryClient();

  return useMutation<Utilisateur, Error, CreerUtilisateurDTO>({
    mutationFn: (data) => utilisateurService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });
    },
  });
};

export const useUpdateUser = () => {
  const queryClient = useQueryClient();

  return useMutation<
    Utilisateur,
    Error,
    { uuid: string; data: Partial<Utilisateur> }
  >({
    mutationFn: ({ uuid, data }) => utilisateurService.update(uuid, data),
    onSuccess: (updatedUser) => {
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: userKeys.detail(updatedUser.uuid),
      });
    },
  });
};

export const useDeleteUser = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => utilisateurService.remove(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });
    },
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
