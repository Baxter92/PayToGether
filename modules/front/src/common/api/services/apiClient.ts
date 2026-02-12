import { ApiClient } from "../module/client/ApiClient";
import { loggerPlugin } from "../module/client/plugins/logger.plugin";

export const apiClient = new ApiClient({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  plugins: [loggerPlugin()],
});
