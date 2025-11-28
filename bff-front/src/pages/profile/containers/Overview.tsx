import { VStack } from "@/common/components";
import Grid from "@/common/components/Grid";
import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import type { JSX } from "react";

export default function Overview(): JSX.Element {
  return (
    <section>
      <Heading
        level={2}
        title="Résumé"
        description="Récapitulatif rapide de ton compte et de tes dernières activités sur la
        plateforme."
        underline
      />

      <Grid cols={{ md: 2, base: 1, lg: 2 }} gap="gap-8" className="mt-4">
        <div className="p-3 border rounded-md">
          <div className="text-xs text-slate-500">Dernier achat</div>
          <div className="font-medium mt-1">Dîner - La Mer</div>
          <div className="text-sm text-slate-500 mt-1">2 jours</div>
        </div>
        <div className="p-3 border rounded-md">
          <div className="text-xs text-slate-500">Deals actifs</div>
          <div className="font-medium mt-1">8 deals</div>
          <div className="text-sm text-slate-500 mt-1">
            Valables sur vos réservations
          </div>
        </div>
      </Grid>

      <VStack className="mt-6">
        <Heading level={3} title="Recommandé pour toi" underline />
        <DealsList
          deals={mockDeals}
          showFilters={false}
          showPagination={false}
          itemsPerPage={3}
          cols={{ md: 3 }}
        />
      </VStack>
    </section>
  );
}
