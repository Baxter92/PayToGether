export interface TokenStorage {
  get(): Promise<string | null> | string | null;
  set(token: string): Promise<void> | void;
  clear(): Promise<void> | void;

  // Optionnel: refresh token helpers (utilisés par plugin refresh)
  getRefresh?(): Promise<string | null> | string | null;
  saveTokens?: (
    accessToken: string,
    refreshToken?: string,
  ) => Promise<void> | void;
}
