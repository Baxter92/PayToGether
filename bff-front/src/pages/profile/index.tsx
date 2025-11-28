import React, { useState } from "react";
import HeaderProfile, { PROFILE_TABS } from "./containers/HeaderProfile";
import { useAuth } from "@/common/context/AuthContext";
import AsideStats from "./containers/AsideStats";
import Overview from "./containers/Overview";
import MyDeals from "./containers/MyDeals";
import MyPurchases from "./containers/MyPurchases";

export type User = {
  id: string;
  name: string;
  email?: string;
  role?: "customer" | "merchant" | string;
  bio?: string;
  location?: string;
  avatarUrl?: string | null;
  savedDeals?: number;
  vouchers?: number;
  walletBalance?: number;
};

const sampleUser: User = {
  id: "u_1",
  name: "Ivan Mbella",
  email: "ivan@example.com",
  role: "customer",
  bio: "Fan des bonnes affaires — j'achète local et je partage les bons plans.",
  location: "Douala, Cameroon",
  avatarUrl: null,
  savedDeals: 18,
  vouchers: 6,
  walletBalance: 12000,
};

type Deal = {
  id: string;
  title: string;
  merchant: string;
  price: string;
  originalPrice?: string;
  purchased?: boolean;
  image?: string | null;
};

const sampleDeals: Deal[] = [
  {
    id: "d1",
    title: "Dîner 2 personnes - Restaurant La Mer",
    merchant: "La Mer",
    price: "₣12,000",
    originalPrice: "₣20,000",
  },
  {
    id: "d2",
    title: "Massage relaxant 60min",
    merchant: "ZenSpa",
    price: "₣8,000",
    originalPrice: "₣15,000",
  },
  {
    id: "d3",
    title: "Cours de cuisine pour 1 personne",
    merchant: "ChefLab",
    price: "₣6,500",
  },
];

export default function Profile({ user = sampleUser }: { user?: User }) {
  // const { user } = useAuth();
  const [activeTab, setActiveTab] =
    useState<(typeof PROFILE_TABS)[number]["key"]>("overview");

  const [deals] = useState<Deal[]>(sampleDeals);

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Header */}
      <HeaderProfile activeTab={activeTab} onTabChange={setActiveTab} />

      {/* Body */}
      <div className="mt-6 grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Left column: quick stats */}
        <AsideStats />

        {/* Main content */}
        <main className="lg:col-span-3 space-y-6">
          {/* Tab contents */}
          <div className="bg-white dark:bg-slate-900 rounded-lg p-6 shadow-sm">
            {activeTab === "overview" && <Overview />}

            {activeTab === "deals" && <MyDeals />}

            {activeTab === "purchases" && <MyPurchases />}

            {activeTab === "favorites" && (
              <section>
                <h2 className="text-lg font-semibold">Offres sauvegardées</h2>
                <p className="text-sm text-slate-600 mt-2">
                  Retrouvez les offres que vous avez mises de coté.
                </p>
              </section>
            )}

            {activeTab === "reviews" && (
              <section>
                <h2 className="text-lg font-semibold">Mes avis</h2>
                <p className="text-sm text-slate-600 mt-2">
                  Historique des avis laissés aux marchands.
                </p>
              </section>
            )}

            {activeTab === "settings" && (
              <section>
                <h2 className="text-lg font-semibold">Paramètres</h2>
                <p className="text-sm text-slate-600 mt-2">
                  Gérer email, mot de passe, notifications et préférences.
                </p>
              </section>
            )}
          </div>

          {/* Activity / feed */}
          <div className="bg-white dark:bg-slate-900 rounded-lg p-6 shadow-sm">
            <h3 className="text-md font-semibold mb-3">Activité récente</h3>
            <ul className="space-y-2 text-sm text-slate-600">
              <li>• Tu as acheté "Dîner 2 personnes" — 2 jours</li>
              <li>• Tu as laissé un avis pour "ZenSpa" — 5 jours</li>
              <li>• Tu as sauvegardé 3 nouvelles offres — 7 jours</li>
            </ul>
          </div>
        </main>
      </div>
    </div>
  );
}
