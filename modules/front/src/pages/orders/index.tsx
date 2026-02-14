import { useDeals } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import type { JSX } from "react";

export default function Orders(): JSX.Element {
  const { data: dealsData, isLoading } = useDeals();
  const deals = (dealsData ?? []).map(mapDealToView);

  return (
    <main className="bg-white">
      <div className="max-w-7xl mx-auto py-4">
        <Heading
          title="Mes commandes"
          description="Historique de vos achats"
          level={2}
          underline
        />
        {isLoading ? (
          <div className="text-center py-8 text-muted-foreground">Chargement...</div>
        ) : (
          <DealsList
            deals={deals}
            cols={{ md: 2, base: 1, lg: 3 }}
            viewMode="list"
            showFilters={false}
            tableProps={{
              enableSelection: false,
            }}
          />
        )}
      </div>
    </main>
  );
}
