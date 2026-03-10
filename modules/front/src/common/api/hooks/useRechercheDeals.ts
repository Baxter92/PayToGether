import { useQuery } from "@tanstack/react-query";
import type { DealRechercheDTO } from "../services/rechercheService";
import { rechercheService } from "../services/rechercheService";
import { useDebounce } from "../../hooks/useDebounce";

/**
 * Hook pour la recherche de deals avec debounce
 * @param query Texte de recherche
 * @param debounceDelay Délai de debounce en ms (défaut: 500ms)
 * @returns Résultats de recherche et états
 */
export const useRechercheDeals = (query: string, debounceDelay = 500) => {
  // Appliquer le debounce sur la query
  const debouncedQuery = useDebounce(query, debounceDelay);

  return useQuery<DealRechercheDTO[], Error>({
    queryKey: ["recherche", "deals", debouncedQuery],
    queryFn: () => rechercheService.rechercherDeals(debouncedQuery),
    enabled: debouncedQuery.trim().length > 0, // Ne pas lancer la recherche si la query est vide
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

