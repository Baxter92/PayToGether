import { useMemo, useState } from "react";
import HeaderProfile, { type PROFILE_TABS } from "./containers/HeaderProfile";
import AsideStats from "./containers/AsideStats";
import Overview from "./containers/Overview";
import MyDeals from "./containers/MyDeals";
import MyPurchases from "./containers/MyPurchases";
import Settings from "./containers/Settings";
import PaymentsList from "./containers/PaymentsList";
import OrdersReceivedList from "./containers/OrderReceivedList";
import { mockOrdersReceived } from "@/common/constants/data";
import ReviewsList, { type Review } from "./containers/ReviewsList";
import Favorites from "./containers/Favorites";
import { useAuth } from "@/common/context/AuthContext";
import {
  type CommentaireDTO,
  useCommentaires,
  useDeals,
  useDealsByCreateur,
  useUsers,
} from "@/common/api";

export default function Profile() {
  const { roles, role, user } = useAuth();
  const [activeTab, setActiveTab] =
    useState<(typeof PROFILE_TABS)[number]["key"]>("overview");
  const { data: commentaires = [], isLoading: commentairesLoading } =
    useCommentaires();
  const { data: deals = [] } = useDeals();
  const { data: users = [] } = useUsers();
  const { data: merchantDeals = [] } = useDealsByCreateur(user?.id ?? "");

  const isMerchant = useMemo(
    () => role === "VENDEUR" || roles.includes("VENDEUR"),
    [role, roles],
  );

  const dealsByUuid = useMemo(
    () => new Map(deals.map((deal) => [deal.uuid, deal.titre])),
    [deals],
  );

  const usersByUuid = useMemo(
    () =>
      new Map(
        users.map((currentUser) => [
          currentUser.uuid,
          {
            name:
              `${currentUser.prenom ?? ""} ${currentUser.nom ?? ""}`.trim() ||
              currentUser.email,
            email: currentUser.email,
          },
        ]),
      ),
    [users],
  );

  const merchantDealUuids = useMemo(
    () => new Set(merchantDeals.map((deal) => deal.uuid)),
    [merchantDeals],
  );

  const commentairesToReviewRows = useMemo(() => {
    const toReview = (commentaire: CommentaireDTO, index: number): Review => {
      const buyer = usersByUuid.get(commentaire.utilisateurUuid);
      return {
        id: commentaire.uuid ?? `${commentaire.utilisateurUuid}-${index}`,
        orderNumber: `COM-${(commentaire.uuid ?? "").slice(0, 8) || index + 1}`,
        dealTitle: dealsByUuid.get(commentaire.dealUuid) ?? "Deal inconnu",
        buyer: {
          id: commentaire.utilisateurUuid,
          name:
            buyer?.name ??
            `Utilisateur ${commentaire.utilisateurUuid?.slice(0, 8)}`,
          email: buyer?.email,
        },
        rating: Number(commentaire.note) || 0,
        comment: commentaire.contenu ?? "",
        status: "published",
        createdAt: commentaire.dateCreation ?? new Date().toISOString(),
      };
    };

    const racines = commentaires
      .filter((commentaire) => !commentaire.commentaireParentUuid)
      .sort(
        (a, b) =>
          new Date(b.dateCreation ?? 0).getTime() -
          new Date(a.dateCreation ?? 0).getTime(),
      );

    const myReviews = racines
      .filter((commentaire) => commentaire.utilisateurUuid === user?.id)
      .map(toReview);

    const clientReviews = racines
      .filter(
        (commentaire) =>
          merchantDealUuids.has(commentaire.dealUuid) &&
          commentaire.utilisateurUuid !== user?.id,
      )
      .map(toReview);

    return { myReviews, clientReviews };
  }, [commentaires, dealsByUuid, merchantDealUuids, user?.id, usersByUuid]);

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

            {activeTab === "reviews" &&
              (commentairesLoading ? (
                <div className="text-sm text-muted-foreground">
                  Chargement des avis...
                </div>
              ) : (
                <ReviewsList
                  data={commentairesToReviewRows.myReviews}
                  isMyReviews
                />
              ))}

            {activeTab === "payouts" && isMerchant && <PaymentsList />}

            {activeTab === "orders-received" && isMerchant && (
              <OrdersReceivedList data={mockOrdersReceived as any} />
            )}

            {activeTab === "client-reviews" &&
              isMerchant &&
              (commentairesLoading ? (
                <div className="text-sm text-muted-foreground">
                  Chargement des avis...
                </div>
              ) : (
                <ReviewsList data={commentairesToReviewRows.clientReviews} />
              ))}

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
