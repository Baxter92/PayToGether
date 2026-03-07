import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { authService, type MeResponse } from "../api/services/authService";
import { localTokenStorage } from "../api/module/client/auth/tokenStorage.web";
import { HttpError } from "../api/module/client/HttpError";
import type { RoleUtilisateurType } from "../api";

export interface IUser {
  id: string;
  username: string;
  email: string;
  prenom?: string;
  nom?: string;
  actif: boolean;
  emailVerifie: boolean;
  dateCreationTimestamp: number;
  roles: RoleUtilisateurType[];
  name: string;
  avatar?: string;
  role: RoleUtilisateurType;
  location?: string;
}

export interface IAuthContextType {
  user: IUser | null;
  roles: RoleUtilisateurType[];
  role: IUser["role"] | null;
  isAdmin: boolean;
  isMerchant: boolean;
  loading: boolean;
  login: (email: string, password: string) => Promise<IUser>;
  register: (
    email: string,
    password: string,
    prenom: string,
    nom: string,
  ) => Promise<void>;
  logout: () => Promise<void>;
  forgotPassword: (email: string) => Promise<void>;
  activateAccount: (token: string) => Promise<void>;
  resetPassword: (token: string, newPassword: string) => Promise<void>;
}

const AuthContext = createContext<IAuthContextType | undefined>(undefined);

export const useAuth = (): IAuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<IUser | null>(null);
  const [roles, setRoles] = useState<RoleUtilisateurType[]>([]);
  const [role, setRole] = useState<IUser["role"] | null>(null);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void checkAuthStatus();
  }, []);

  const mapRole = (roles: string[] | undefined): IUser["role"] => {
    const normalized = (roles ?? []).map((role) => role.toUpperCase());

    if (normalized.includes("ADMIN") || normalized.includes("ROLE_ADMIN")) {
      return "ADMIN";
    }

    if (
      normalized.includes("MARCHAND") ||
      normalized.includes("ROLE_MARCHAND") ||
      normalized.includes("MERCHANT") ||
      normalized.includes("ROLE_MERCHANT")
    ) {
      return "VENDEUR";
    }

    return "UTILISATEUR";
  };

  const mapMeToUser = (me: MeResponse): IUser => {
    const fullName = `${me.prenom ?? ""} ${me.nom ?? ""}`.trim();
    const fallbackName = me.username || me.email.split("@")[0];
    const name = fullName || fallbackName;

    return {
      id: me.id,
      username: me.username,
      email: me.email,
      prenom: me.prenom,
      nom: me.nom,
      actif: me.actif,
      emailVerifie: me.emailVerifie,
      dateCreationTimestamp: me.dateCreationTimestamp,
      roles: me.roles ?? [],
      name,
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(
        me.email || me.username || me.id,
      )}`,
      role: mapRole(me.roles),
    };
  };

  const { isMerchant, isAdmin } = useMemo(
    () => ({
      isMerchant: role === "VENDEUR" || roles.includes("VENDEUR"),
      isAdmin: role === "ADMIN" || roles.includes("ADMIN"),
    }),
    [role, roles],
  );

  const toAuthErrorMessage = (error: unknown): string => {
    if (error instanceof HttpError) {
      if (error.status === 401) return "auth:invalidCredentials";
      if (error.status >= 500) return "auth:serverError";
      return "auth.loginError";
    }

    if (error instanceof Error) return "auth.loginError";
    return "auth.authError";
  };

  const checkAuthStatus = async (): Promise<void> => {
    try {
      const token = await localTokenStorage.get();
      if (!token) {
        setUser(null);
        return;
      }

      const me = await authService.me();
      const userData = mapMeToUser(me);
      setUser(userData);
      setRoles(userData.roles);
      setRole(userData.role);
    } catch (error) {
      console.error("Auth check failed:", error);
      await localTokenStorage.clear();
      setUser(null);
      setRoles([]);
      setRole(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string): Promise<IUser> => {
    try {
      if (!email || !password) {
        throw new Error("auth:invalidCredentials");
      }

      const loginResponse = await authService.login(email, password);
      if (localTokenStorage.saveTokens) {
        await localTokenStorage.saveTokens(
          loginResponse.accessToken,
          loginResponse.refreshToken,
        );
      } else {
        await localTokenStorage.set(loginResponse.accessToken);
      }

      const me = await authService.me();
      const userData = mapMeToUser(me);
      setUser(userData);
      setRoles(userData.roles);
      setRole(userData.role);
      return userData;
    } catch (error) {
      throw new Error(toAuthErrorMessage(error));
    }
  };

  const register = async (
    email: string,
    password: string,
    prenom: string,
    nom: string,
  ): Promise<void> => {
    try {
      await authService.register({
        prenom: prenom.trim(),
        nom: nom.trim(),
        email,
        motDePasse: password,
        role: "UTILISATEUR",
      });
    } catch (error) {
      if (error instanceof HttpError) {
        if (error.status === 409) throw new Error("auth:registerEmailExists");
        if (error.status === 400) throw new Error("auth:registerError");
        if (error.status >= 500) throw new Error("auth:serverError");
      }
      throw new Error("auth:registerError");
    }
  };

  const logout = async (): Promise<void> => {
    try {
      const token = await localTokenStorage.get();
      if (token) {
        await authService.logout();
      }
    } catch (error) {
      console.error("Logout failed:", error);
    } finally {
      await localTokenStorage.clear();
      setUser(null);
    }
  };

  const forgotPassword = async (email: string): Promise<void> => {
    try {
      await authService.forgotPassword(email);
    } catch (error) {
      console.error("Forgot password failed:", error);
      throw new Error("auth:forgotPasswordError");
    }
  };

  const resetPassword = async (
    token: string,
    newPassword: string,
  ): Promise<void> => {
    try {
      await authService.resetPassword(token, newPassword);
    } catch (error) {
      console.error("Reset password failed:", error);
      throw new Error("auth:resetPasswordError");
    }
  };

  const activateAccount = async (token: string): Promise<void> => {
    try {
      await authService.activateAccount(token);
    } catch (error) {
      console.error("Account activation failed:", error);
      throw new Error("auth:activateAccountError");
    }
  };

  const value: IAuthContextType = {
    user,
    loading,
    login,
    register,
    logout,
    roles,
    role,
    isAdmin,
    isMerchant,
    forgotPassword,
    activateAccount,
    resetPassword,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
