import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { merchantService } from "../services/merchantService";
import { toast } from "sonner";

/**
 * Hook pour récupérer tous les marchands avec leurs statistiques
 */
export const useGetAllSellers = () => {
  return useQuery({
    queryKey: ["sellers"],
    queryFn: () => merchantService.getAllSellers(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook pour suspendre un marchand
 */
export const useSuspendSeller = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (uuid: string) => merchantService.suspendSeller(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["sellers"] });
      toast.success("Marchand suspendu avec succès");
    },
    onError: (error: any) => {
      toast.error(
        error?.response?.data?.message || "Erreur lors de la suspension du marchand",
      );
    },
  });
};

/**
 * Hook pour activer un marchand
 */
export const useActivateSeller = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (uuid: string) => merchantService.activateSeller(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["sellers"] });
      toast.success("Marchand activé avec succès");
    },
    onError: (error: any) => {
      toast.error(
        error?.response?.data?.message || "Erreur lors de l'activation du marchand",
      );
    },
  });
};

