import { VStack } from "@/common/components";
import { useCategories } from "@/common/api";
import { mapCategoryToView } from "@/common/api/mappers/catalog";
import CategoriesList from "@/common/containers/CategoriesList";
import { type JSX } from "react";

export default function Categories(): JSX.Element {
  const { data: categoriesData, isLoading } = useCategories();
  const categories = (categoriesData ?? []).map(mapCategoryToView);

  return (
    <VStack className="p-4">
      {isLoading ? (
        <div className="text-center py-8 text-muted-foreground">Chargement...</div>
      ) : (
        <CategoriesList categories={categories} />
      )}
    </VStack>
  );
}
