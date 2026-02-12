import type { TokenStorage } from "../auth/TokenStorage";
import type { ApiPlugin } from "../types";

export const authPlugin = (storage: TokenStorage): ApiPlugin => ({
  onRequest: async (ctx) => {
    const token = await storage.get();
    if (token) ctx.headers.Authorization = `Bearer ${token}`;
  },
});
