import React, { useState } from "react";
import HeaderProfile, { type PROFILE_TABS } from "./containers/HeaderProfile";
import AsideStats from "./containers/AsideStats";
import Overview from "./containers/Overview";
import MyDeals from "./containers/MyDeals";
import MyPurchases from "./containers/MyPurchases";
import Settings from "./containers/Settings";
import PaymentsList from "./containers/PaymentsList";
import OrdersReceivedList from "./containers/OrderReceivedList";
import { mockOrdersReceived } from "@/common/constants/data";
import ReviewsList from "./containers/ReviewsList";

export default function Profile() {
  // const { user } = useAuth();
  const [activeTab, setActiveTab] =
    useState<(typeof PROFILE_TABS)[number]["key"]>("overview");

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

            {activeTab === "reviews" && <ReviewsList />}

            {activeTab === "payouts" && <PaymentsList />}

            {activeTab === "orders-received" && (
              <OrdersReceivedList data={mockOrdersReceived as any} />
            )}

            {activeTab === "client-reviews" && <ReviewsList />}

            {activeTab === "settings" && <Settings />}
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
