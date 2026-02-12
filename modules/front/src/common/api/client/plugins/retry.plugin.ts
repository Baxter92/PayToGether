import type { ApiPlugin } from "../types";

// Note: ApiClient inclut la logique de retry interne.
// Ce plugin sert à logger / métriques quand un retry se produit.
export const retryPlugin = (opts?: {
  onRetry?: (attempt: number, err: any) => void;
}): ApiPlugin => {
  return {
    onError: (err) => {
      // possibilité d'envoyer metrics
      return undefined;
    },
  };
};
