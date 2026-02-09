import { useMemo, useState } from "react";
import { useI18n } from "@hooks/useI18n";
import HeaderProfile, { type PROFILE_TABS } from "./containers/HeaderProfile";
import AsideStats from "./containers/AsideStats";
import Overview from "./containers/Overview";
import MyDeals from "./containers/MyDeals";
import MyPurchases from "./containers/MyPurchases";
import Settings from "./containers/Settings";
import PaymentsList from "./containers/PaymentsList";
import OrdersReceivedList from "./containers/OrderReceivedList";
import { mockOrdersReceived, mockReviews } from "@/common/constants/data";
import ReviewsList, { type Review } from "./containers/ReviewsList";
import Favorites from "./containers/Favorites";
import { useAuth } from "@/common/context/AuthContext";

export default function Profile() {
  const { t } = useI18n("profile");
  const { user } = useAuth();
  const [activeTab, setActiveTab] =
    useState<(typeof PROFILE_TABS)[number]["key"]>("overview");

  const isMerchant = useMemo(() => user?.role === "marchand", [user]);
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

            {activeTab === "deals" && isMerchant && <MyDeals />}

            {activeTab === "purchases" && <MyPurchases />}

            {activeTab === "favorites" && <Favorites />}

            {activeTab === "reviews" && (
              <ReviewsList data={mockReviews as Review[]} isMyReviews />
            )}

            {activeTab === "payouts" && isMerchant && <PaymentsList />}

            {activeTab === "orders-received" && isMerchant && (
              <OrdersReceivedList data={mockOrdersReceived as any} />
            )}

            {activeTab === "client-reviews" && isMerchant && (
              <ReviewsList data={mockReviews as Review[]} />
            )}

            {activeTab === "settings" && <Settings />}
          </div>

          {/* Activity / feed */}
          <div className="bg-white dark:bg-slate-900 rounded-lg p-6 shadow-sm">
            <h3 className="text-md font-semibold mb-3">
              {t("profile.recentActivity")}
            </h3>
            <ul className="space-y-2 text-sm text-slate-600">
              <li>
                •{" "}
                {t("profile.boughtDeal", {
                  deal: "Dîner 2 personnes",
                  days: 2,
                })}
              </li>
              <li>
                • {t("profile.leftReview", { merchant: "ZenSpa", days: 5 })}
              </li>
              <li>• {t("profile.savedOffers", { count: 3, days: 7 })}</li>
            </ul>
          </div>
        </main>
      </div>
    </div>
  );
}
