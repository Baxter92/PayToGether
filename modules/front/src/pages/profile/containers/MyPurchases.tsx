import { useTranslation } from "react-i18next";
import { useDeals } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { type JSX } from "react";

export default function MyPurchases(): JSX.Element {
  const { t } = useTranslation();
  const { data: dealsData } = useDeals();
  const deals = (dealsData ?? []).map(mapDealToView);

  return (
    <section>
      <Heading
        level={2}
        title={t("profile.purchases")}
        description={t("profile.purchasesDescription")}
        underline
      />

      <DealsList deals={deals} viewMode="list" showFilters={false} />
    </section>
  );
}
