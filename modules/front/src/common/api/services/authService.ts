import type { RoleUtilisateurType } from "../types";
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
  roles: RoleUtilisateurType[];
};

export type RegisterRequest = {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  role: RoleUtilisateurType;
  photoProfil?: string;
};

export const authService = {
  login: (username: string, password: string) =>
    apiClient.post<LoginResponse>("/auth/login", {
      body: {
        username,
        password,
      },
    }),
  register: (payload: RegisterRequest) =>
    apiClient.post<void>("/auth/register", { body: payload }),
  me: () => apiClient.get<MeResponse>("/auth/me"),
  logout: () => apiClient.post<void>("/auth/logout"),
  forgotPassword: (email: string) =>
    apiClient.post<void>("/auth/forgot-password", { body: { email } }),
  activateAccount: (token: string) =>
    apiClient.get<void>("/auth/activate-account", {
      queryParams: { token },
    }),
  resetPassword: (token: string, newPassword: string) =>
    apiClient.post<void>("/auth/reset-password", {
      body: {
        token,
        nouveauMotDePasse: newPassword,
      },
    }),
};
