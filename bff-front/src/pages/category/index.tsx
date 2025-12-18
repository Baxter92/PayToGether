import { VStack } from "@/common/components";
import { categories, mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { useMemo, type JSX } from "react";
import { useParams } from "react-router-dom";

export default function Category(): JSX.Element {
  const { id } = useParams();
  const category = useMemo(() => {
    return categories.find((c) => c.href === id);
  }, [id]);
  return (
    <VStack className="p-4">
      <DealsList
        deals={mockDeals}
        showTitle
        title={category?.name}
        description={category?.description}
        viewModeToggleable
      />
    </VStack>
  );
}
