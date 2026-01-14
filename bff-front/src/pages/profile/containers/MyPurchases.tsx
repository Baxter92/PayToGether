import { useTranslation } from "react-i18next";
import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { type JSX } from "react";

export default function MyPurchases(): JSX.Element {
  const { t } = useTranslation();

  return (
    <section>
      <Heading
        level={2}
        title={t("profile.purchases")}
        description={t("profile.purchasesDescription")}
        underline
      />

      <DealsList deals={mockDeals} viewMode="list" showFilters={false} />
    </section>
  );
}
