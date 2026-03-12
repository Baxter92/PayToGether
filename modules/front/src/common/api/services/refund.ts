import { apiClient } from "./apiClient";

export interface RemboursementEnMasseDTO {
  dealUuid: string;
  utilisateurUuids: string[];
  raisonRemboursement?: string;
}

export interface RemboursementEnMasseResponseDTO {
  dealUuid: string;
  nombreUtilisateurs: number;
  nombreRemboursementsReussis: number;
  nombreEchecs: number;
  message: string;
  details: string[];
}

/**
 * Rembourse plusieurs participants en masse (admin uniquement)
 */
export async function refundParticipantsBulk(
  data: RemboursementEnMasseDTO,
): Promise<RemboursementEnMasseResponseDTO> {
  return apiClient.post<RemboursementEnMasseResponseDTO>(
    "/square-payments/refund-bulk",
    { body: data },
  );
}

