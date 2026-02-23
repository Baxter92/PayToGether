import { useI18n } from "@hooks/useI18n";
import { VStack } from "@/common/components";
import Grid from "@/common/components/Grid";
import { useDealsByStatut } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import type { JSX } from "react";

export default function Overview(): JSX.Element {
  const { t } = useI18n("profile");
  const { data: dealsData } = useDealsByStatut(StatutDeal.PUBLIE);
  const deals = (dealsData ?? []).map(mapDealToView);

  return (
    <section>
      <Heading
        level={2}
        title={t("summary")}
        description={t("summaryDescription")}
        underline
      />

      <Grid cols={{ md: 2, base: 1, lg: 2 }} gap="gap-8" className="mt-4">
        <div className="p-3 border rounded-md">
          <div className="text-xs text-slate-500">{t("lastPurchase")}</div>
          <div className="font-medium mt-1">Dîner - La Mer</div>
          <div className="text-sm text-slate-500 mt-1">2 {t("daysAgo")}</div>
        </div>
        <div className="p-3 border rounded-md">
          <div className="text-xs text-slate-500">{t("activeDeals")}</div>
          <div className="font-medium mt-1">8 deals</div>
          <div className="text-sm text-slate-500 mt-1">
            {t("validOnBookings")}
          </div>
        </div>
      </Grid>

      <VStack className="mt-6">
        <Heading level={3} title={t("recommendedForYou")} underline />
        <DealsList
          deals={deals}
          showFilters={false}
          showPagination={false}
          itemsPerPage={3}
          cols={{ md: 3 }}
        />
      </VStack>
    </section>
  );
}
