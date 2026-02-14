import { VStack } from "@/common/components";
import { useCategories, useDeals } from "@/common/api";
import { mapCategoryToView, mapDealToView } from "@/common/api/mappers/catalog";
import DealsList from "@/common/containers/DealList";
import { useMemo, type JSX } from "react";
import { useParams } from "react-router-dom";

export default function Category(): JSX.Element {
  const { id } = useParams();
  const { data: categoriesData, isLoading: isLoadingCategories } =
    useCategories();
  const { data: dealsData, isLoading: isLoadingDeals } = useDeals();

  const categories = (categoriesData ?? []).map(mapCategoryToView);
  const deals = (dealsData ?? []).map(mapDealToView);

  const category = useMemo(() => {
    return categories.find((c) => String(c.href) === String(id));
  }, [categories, id]);

  const filteredDeals = useMemo(() => {
    if (!category) return deals;
    return deals.filter(
      (deal: any) =>
        deal.raw?.categorieUuid === category.id ||
        deal.category === category.name,
    );
  }, [deals, category]);

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
