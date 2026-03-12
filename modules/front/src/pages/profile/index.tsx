import { useState } from "react";
import HeaderProfile, { type PROFILE_TABS } from "./containers/HeaderProfile";
import AsideStats from "./containers/AsideStats";
import Overview from "./containers/Overview";
import MyDeals from "./containers/MyDeals";
import MyPurchases from "./containers/MyPurchases";
import Settings from "./containers/Settings";
import Favorites from "./containers/Favorites";
import { useAuth } from "@/common/context/AuthContext";

export default function Profile() {
  const { isMerchant, isAdmin } = useAuth();
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

            {activeTab === "deals" && (isMerchant || isAdmin) && <MyDeals />}

            {activeTab === "payouts" && <MyPurchases />}

            {activeTab === "favorites" && <Favorites />}

            {/* {activeTab === "reviews" &&
              (commentairesLoading ? (
                <div className="text-sm text-muted-foreground">
                  Chargement des avis...
                </div>
              ) : (
                <ReviewsList
                  data={commentairesToReviewRows.myReviews}
                  isMyReviews
                />
              ))} */}

            {/* {activeTab === "payouts" && (isMerchant || isAdmin) && (
              <PaymentsList />
            )} */}

            {/* {activeTab === "orders-received" && (isMerchant || isAdmin) && (
              <OrdersReceivedList data={mockOrdersReceived as any} />
            )} */}

            {/* {activeTab === "client-reviews" &&
              (isMerchant || isAdmin) &&
              (commentairesLoading ? (
                <div className="text-sm text-muted-foreground">
                  Chargement des avis...
                </div>
              ) : (
                <ReviewsList data={commentairesToReviewRows.clientReviews} />
              ))} */}

            {activeTab === "settings" && <Settings />}
          </div>

          {/* Activity / feed */}
          {/* <div className="bg-white dark:bg-slate-900 rounded-lg p-6 shadow-sm">
            <h3 className="text-md font-semibold mb-3">
              {t("recentActivity")}
            </h3>
            <ul className="space-y-2 text-sm text-slate-600">
              <li>
                •{" "}
                {t("boughtDeal", {
                  deal: "Dîner 2 personnes",
                  days: 2,
                })}
              </li>
              <li>• {t("leftReview", { merchant: "ZenSpa", days: 5 })}</li>
              <li>• {t("savedOffers", { count: 3, days: 7 })}</li>
            </ul>
          </div> */}
        </main>
      </div>
    </div>
  );
}
