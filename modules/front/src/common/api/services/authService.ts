import { apiClient } from "./apiClient";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number;
  scope: string;
};

export type MeResponse = {
  id: string;
  username: string;
  email: string;
  prenom?: string;
  nom?: string;
  actif: boolean;
  emailVerifie: boolean;
  dateCreationTimestamp: number;
  roles: string[];
};

export const authService = {
  login: (username: string, password: string) =>
    apiClient.post<LoginResponse>("/auth/login", {
      body: {
        username,
        password,
      },
    }),
  me: () => apiClient.get<MeResponse>("/auth/me"),
  logout: () => apiClient.post<void>("/auth/logout"),
};
