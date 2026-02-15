import { VStack } from "@/common/components";
import { useCategories, useDealsByCategorie } from "@/common/api";
import { mapCategoryToView, mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import DealsList from "@/common/containers/DealList";
import { useMemo, type JSX } from "react";
import { useParams } from "react-router-dom";

export default function Category(): JSX.Element {
  const { id } = useParams();
  const { data: categoriesData, isLoading: isLoadingCategories } =
    useCategories();
  const { data: dealsData, isLoading: isLoadingDeals } = useDealsByCategorie(
    String(id ?? ""),
  );

  const categories = (categoriesData ?? []).map(mapCategoryToView);
  const deals = (dealsData ?? []).map(mapDealToView);

  const category = useMemo(() => {
    return categories.find((c) => String(c.href) === String(id));
  }, [categories, id]);

  const filteredDeals = useMemo(() => {
    return deals.filter((deal: any) => deal.raw?.statut === StatutDeal.PUBLIE);
  }, [deals]);

  return (
    <VStack className="p-4">
      {isLoadingCategories || isLoadingDeals ? (
        <div className="text-center py-8 text-muted-foreground">Chargement...</div>
      ) : (
        <DealsList
          deals={filteredDeals}
          showTitle
          title={category?.name}
          description={category?.description}
          viewModeToggleable
        />
      )}
    </VStack>
  );
}
