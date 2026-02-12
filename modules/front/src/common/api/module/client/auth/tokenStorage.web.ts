// src/auth/tokenStorage.web.ts
import type { TokenStorage } from "./TokenStorage";
const KEY = "authToken";
const REFRESH_KEY = "refreshToken";

export const localTokenStorage: TokenStorage = {
  get: () => localStorage.getItem(KEY),
  set: (t) => localStorage.setItem(KEY, t),
  clear: () => {
    localStorage.removeItem(KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  saveTokens: async (access, refresh) => {
    localStorage.setItem(KEY, access);
    if (refresh) localStorage.setItem(REFRESH_KEY, refresh);
  },
};
