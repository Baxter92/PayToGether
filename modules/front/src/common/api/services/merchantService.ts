import { apiClient } from "../services/apiClient";
import type { MarchandsAvecStatistiques } from "../types/merchant.types";

/**
 * Service API pour les marchands
 */
export const merchantService = {
  /**
   * Récupère tous les marchands avec leurs deals et statistiques
   */
  getAllSellers: async (): Promise<MarchandsAvecStatistiques> => {
    const response = await apiClient.get<MarchandsAvecStatistiques>("/utilisateurs/sellers");
    return response;
  },

  /**
   * Suspend un marchand (le désactive)
   */
  suspendSeller: async (uuid: string): Promise<void> => {
    await apiClient.patch(`/utilisateurs/${uuid}/disable`);
  },

  /**
   * Active un marchand
   */
  activateSeller: async (uuid: string): Promise<void> => {
    await apiClient.patch(`/utilisateurs/${uuid}/enable`);
  },
};

