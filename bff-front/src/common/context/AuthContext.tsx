import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";

export interface IUser {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  role?: "client" | "marchand";
  location?: string;
}

export interface IAuthContextType {
  user: IUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string) => Promise<void>;
  logout: () => void;
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
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      // Remplacer par: const token = document.cookie.includes('auth_token');
      // Puis faire un appel API pour valider le token
      // const response = await fetch('/api/auth/me', { credentials: 'include' });

      setLoading(false);
    } catch (error) {
      console.error("Auth check failed:", error);
      setLoading(false);
    }
  };

  const login = async (email: string, password: string): Promise<void> => {
    try {
      // Simulation - Remplacer par votre appel API
      await new Promise((resolve) => setTimeout(resolve, 1000));

      if (!email || password.length < 6) {
        throw new Error("Identifiants invalides");
      }

      // const response = await fetch('/api/auth/login', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify({ email, password }),
      //   credentials: 'include' // Important pour les cookies
      // });

      const userData: IUser = {
        id: "1",
        email,
        name: email.split("@")[0],
        avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${email}`,
        role: "client",
        location: "Douala, Cameroon",
      };

      setUser(userData);
    } catch (error) {
      throw error;
    }
  };

  const register = async (
    email: string,
    password: string,
    name: string
  ): Promise<void> => {
    try {
      await new Promise((resolve) => setTimeout(resolve, 1000));

      if (!email || password.length < 6 || !name) {
        throw new Error("Données invalides");
      }

      // const response = await fetch('/api/auth/register', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify({ email, password, name }),
      //   credentials: 'include'
      // });

      const userData: IUser = {
        id: Date.now().toString(),
        email,
        name,
        avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${email}`,
        role: "client",
      };

      setUser(userData);
    } catch (error) {
      throw error;
    }
  };

  const logout = (): void => {
    // await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    setUser(null);
  };

  const value: IAuthContextType = {
    user,
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
