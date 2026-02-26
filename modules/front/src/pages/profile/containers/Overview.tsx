import { useI18n } from "@hooks/useI18n";
import Grid from "@/common/components/Grid";
import { useDealsByCreateur } from "@/common/api";
import type { JSX } from "react";
import { useAuth } from "@/common/context/AuthContext";
import { Heading } from "@/common/containers/Heading";

export default function Overview(): JSX.Element {
  const { t } = useI18n("profile");
  const { user } = useAuth();
  const { data: dealsData } = useDealsByCreateur(user?.id ?? "");
  // const deals = (dealsData ?? []).map(mapDealToView);

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
          <div className="font-medium mt-1">{dealsData?.length ?? 0} deals</div>
        </div>
      </Grid>

      {/* <VStack className="mt-6">
        <Heading level={3} title={t("recommendedForYou")} underline />
        <DealsList
          deals={deals}
          showFilters={false}
          showPagination={false}
          itemsPerPage={3}
          cols={{ md: 3 }}
        />
      </VStack> */}
    </section>
  );
}
