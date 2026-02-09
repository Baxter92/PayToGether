import { useEffect } from "react";
import { useI18n } from "@hooks/useI18n";
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
  {
    labelKey: "profile.overviewTab",
    key: "overview",
    icon: Home,
    merchant: false,
  },
  {
    labelKey: "profile.purchases",
    key: "purchases",
    icon: ShoppingBag,
    merchant: false,
  },
  {
    labelKey: "profile.favorites",
    key: "favorites",
    icon: Heart,
    merchant: false,
  },
  { labelKey: "profile.reviews", key: "reviews", icon: Star, merchant: false },
  { labelKey: "profile.deals", key: "deals", icon: Gift, merchant: true },
  {
    labelKey: "profile.ordersReceived",
    key: "orders-received",
    icon: Store,
    merchant: true,
  },
  {
    labelKey: "profile.clientReviews",
    key: "client-reviews",
    icon: Star,
    merchant: true,
  },
  {
    labelKey: "profile.payouts",
    key: "payouts",
    icon: ShoppingBag,
    merchant: true,
  },
  {
    labelKey: "profile.settings",
    key: "settings",
    icon: Settings,
    merchant: false,
  }
] as const;

export default function HeaderProfile({
  activeTab = "overview",
  onTabChange,
}: {
  activeTab?: (typeof PROFILE_TABS)[number]["key"];
  onTabChange?: (key: (typeof PROFILE_TABS)[number]["key"]) => void;
}): JSX.Element {
  const { t } = useI18n("profile");
  const { user } = useAuth();

  useEffect(() => {
    // 1. Lire le hash au chargement
    const hash = window.location.hash.replace("#", "");

    if (hash) {
      const exists = PROFILE_TABS.some((tab) => tab.key === hash);
      if (exists) {
        onTabChange?.(hash as any);
      }
    }

    // 2. Ecoute les changements manuels du hash
    const handleHashChange = () => {
      const newHash = window.location.hash.replace("#", "");
      const exists = PROFILE_TABS.some((tab) => tab.key === newHash);
      if (exists) {
        onTabChange?.(newHash as any);
      }
    };

    window.addEventListener("hashchange", handleHashChange);

    return () => window.removeEventListener("hashchange", handleHashChange);
  }, [onTabChange]);

  // 3. Mettre à jour l'URL quand un onglet est cliqué
  const handleTabClick = (key: string) => {
    window.location.hash = key; // met à jour l'URL
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
            {user?.role === "marchand"
              ? t("profile.merchant")
              : t("profile.client")}{" "}
            • {user?.location}
          </p>
          <p className="text-sm text-slate-500 dark:text-slate-300 mt-1">
            {user?.email}
          </p>
        </div>
      </div>

      <HStack spacing={8} wrap>
        {PROFILE_TABS.map(({ labelKey, key, icon: Icon, merchant }) => {
          if (merchant && user?.role === "marchand") {
            return (
              <Button
                key={key}
                leftIcon={<Icon className="w-4 h-4" />}
                variant={activeTab === key ? "default" : "secondary"}
                onClick={() => handleTabClick(key)}
              >
                {t(labelKey)}
              </Button>
            );
          } else if (!merchant) {
            return (
              <Button
                key={key}
                leftIcon={<Icon className="w-4 h-4" />}
                variant={activeTab === key ? "default" : "secondary"}
                onClick={() => handleTabClick(key)}
              >
                {t(labelKey)}
              </Button>
            );
          } else {
            return <></>;
          }
        })}
      </HStack>
    </div>
  );
}
