import { useTranslation } from "react-i18next";
import { useDealsByStatut } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { type JSX } from "react";

export default function MyPurchases(): JSX.Element {
  const { t } = useTranslation();
  const { data: dealsData } = useDealsByStatut(StatutDeal.PUBLIE);
  const deals = (dealsData ?? []).map(mapDealToView);

  return (
    <section>
      <Heading
        level={2}
        title={t("purchases")}
        description={t("purchasesDescription")}
        underline
      />

      <DealsList deals={deals} viewMode="list" showFilters={false} />
    </section>
  );
}
