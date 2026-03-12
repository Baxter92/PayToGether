import { useMutation, useQueryClient, type UseMutationResult } from "@tanstack/react-query";
import {
  refundParticipantsBulk,
  type RemboursementEnMasseDTO,
  type RemboursementEnMasseResponseDTO,
} from "../services/refund";
import { toast } from "sonner";

/**
 * Hook pour rembourser plusieurs participants en masse (admin uniquement)
 */
export function useRefundParticipantsBulk(): UseMutationResult<
  RemboursementEnMasseResponseDTO,
  Error,
  RemboursementEnMasseDTO
  > {
  const queryClient = useQueryClient();

  return useMutation<RemboursementEnMasseResponseDTO, Error, RemboursementEnMasseDTO>({
    mutationFn: refundParticipantsBulk,
    onSuccess: (data) => {
      // Invalider les caches des participants et paiements
      queryClient.invalidateQueries({ queryKey: ["deal-participants"] });
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      queryClient.invalidateQueries({ queryKey: ["payments"] });

      // Message de succès
      toast.success(`${data.nombreRemboursementsReussis}/${data.nombreUtilisateurs} refunds processed successfully`);

      if (data.nombreEchecs > 0) {
        toast.warning(`${data.nombreEchecs} refunds failed`);
      }
    },
    onError: (error) => {
      toast.error(`Refund error: ${error.message}`);
    },
  });
}

