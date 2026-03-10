import { apiClient } from "./apiClient";

/**
 * Type pour les résultats de recherche de deals
 */
export interface DealRechercheDTO {
  uuid: string;
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  statut: string;
  ville: string;
  pays: string;
  categorieUuid: string;
  categorieNom: string;
  createurUuid: string;
  createurNom: string;
  imagePrincipaleUrl: string;
  nombreDeVues: number;
  dateCreation: string;
}

/**
 * Service pour la recherche globale de deals
 */
export const rechercheService = {
  /**
   * Recherche globale de deals par texte
   * @param query Texte de recherche
   * @returns Liste de deals correspondants
   */
  rechercherDeals: async (query: string): Promise<DealRechercheDTO[]> => {
    return await apiClient.get<DealRechercheDTO[]>(
      `/recherche/deals?q=${encodeURIComponent(query)}`,
    );
  },
};

