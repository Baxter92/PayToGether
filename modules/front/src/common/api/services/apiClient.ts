import { ApiClient } from "../module/client/ApiClient";
import { localTokenStorage } from "../module/client/auth/tokenStorage.web";
import { authPlugin } from "../module/client/plugins/auth.plugin";
import { loggerPlugin } from "../module/client/plugins/logger.plugin";

// Utilise VITE_API_DEV_BASE_URL en dev, VITE_API_BASE_URL en prod
const baseURL = import.meta.env.DEV
  ? import.meta.env.VITE_API_DEV_BASE_URL
  : import.meta.env.VITE_API_BASE_URL;

export const apiClient = new ApiClient({
  baseURL,
  plugins: [loggerPlugin(), authPlugin(localTokenStorage)],
  // Timeout augmenté à 60s (défaut était 30s) pour les opérations longues
  // comme la création d'utilisateur qui appelle Keycloak en externe
  defaultTimeoutMs: 60_000,
});
