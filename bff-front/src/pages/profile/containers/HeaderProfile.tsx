import { useEffect } from "react";
import { HStack } from "@/common/components";
import { Avatar, AvatarFallback } from "@/common/components/ui/avatar";
import { Button } from "@/common/components/ui/button";
import { useAuth } from "@/common/context/AuthContext";
import { AvatarImage } from "@radix-ui/react-avatar";
import {
  Gift,
  Heart,
  Home,
  Settings,
  ShoppingBag,
  Star,
  Store,
} from "lucide-react";
import { type JSX } from "react";

export const PROFILE_TABS = [
  { label: "Overview", key: "overview", icon: Home },
  { label: "Achats", key: "purchases", icon: ShoppingBag },
  { label: "Favoris", key: "favorites", icon: Heart },
  { label: "Avis", key: "reviews", icon: Star },
  { label: "Mes Deals", key: "deals", icon: Gift },
  { label: "Commandes recues", key: "orders-received", icon: Store },
  { label: "Avis Client", key: "client-reviews", icon: Star },
  { label: "Payments", key: "payouts", icon: ShoppingBag },
  { label: "Paramètres", key: "settings", icon: Settings },
] as const;

export default function HeaderProfile({
  activeTab = "overview",
  onTabChange,
}: {
  activeTab?: (typeof PROFILE_TABS)[number]["key"];
  onTabChange?: (key: (typeof PROFILE_TABS)[number]["key"]) => void;
}): JSX.Element {
  const { user } = useAuth();

  useEffect(() => {
    // 1. Lire le hash au chargement
    const hash = window.location.hash.replace("#", "");

    if (hash) {
      const exists = PROFILE_TABS.some((t) => t.key === hash);
      if (exists) {
        onTabChange?.(hash as any);
      }
    }

    // 2. Ecoute les changements manuels du hash
    const handleHashChange = () => {
      const newHash = window.location.hash.replace("#", "");
      const exists = PROFILE_TABS.some((t) => t.key === newHash);
      if (exists) {
        onTabChange?.(newHash as any);
      }
    };

    window.addEventListener("hashchange", handleHashChange);

    return () => window.removeEventListener("hashchange", handleHashChange);
  }, [onTabChange]);

  // 3. Mettre à jour l’URL quand un onglet est cliqué
  const handleTabClick = (key: string) => {
    window.location.hash = key; // met à jour l’URL
    onTabChange?.(key as any);
  };

  return (
    <div className="bg-white dark:bg-slate-900 rounded-2xl shadow p-6 flex flex-col lg:flex-row gap-6 lg:items-center">
      <div className="flex items-center gap-4">
        <Avatar className="h-20 w-20">
          <AvatarImage src={user?.avatar} alt={user?.name} />
          <AvatarFallback>{user?.name[0]?.toUpperCase?.()}</AvatarFallback>
        </Avatar>

        <div>
          <h1 className="text-xl font-semibold text-slate-900 dark:text-slate-100">
            {user?.name?.capitalizeWords?.()}
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-300">
            {user?.role === "marchand" ? "Marchand" : "Client"} •{" "}
            {user?.location}
          </p>
          <p className="text-sm text-slate-500 dark:text-slate-300 mt-1">
            {user?.email}
          </p>
        </div>
      </div>

      <HStack spacing={8} wrap>
        {PROFILE_TABS.map(({ label, key, icon: Icon }) => (
          <Button
            key={key}
            leftIcon={<Icon className="w-4 h-4" />}
            variant={activeTab === key ? "default" : "secondary"}
            onClick={() => handleTabClick(key)}
          >
            {label}
          </Button>
        ))}
      </HStack>
    </div>
  );
}
