import type { ApiPlugin } from "../types";

// Note: ApiClient inclut la logique de retry interne.
// Ce plugin sert à logger / métriques quand un retry se produit.
export const retryPlugin = (opts?: {
  onRetry?: (attempt: number, err: any) => void;
}): ApiPlugin => {
  console.log(opts, "retry plugin");

  return {
    onError: (err) => {
      // possibilité d'envoyer metrics
      console.log(err, "retry plugin");

      return undefined;
    },
  };
};
