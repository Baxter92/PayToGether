import { ApiClient } from "../module/client/ApiClient";
import { localTokenStorage } from "../module/client/auth/tokenStorage.web";
import { authPlugin } from "../module/client/plugins/auth.plugin";
import { loggerPlugin } from "../module/client/plugins/logger.plugin";

export const apiClient = new ApiClient({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  plugins: [loggerPlugin(), authPlugin(localTokenStorage)],
});
