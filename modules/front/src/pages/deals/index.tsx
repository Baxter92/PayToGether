import { useDeals } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import DealsList from "@/common/containers/DealList";

export default function Deals() {
  const { data: dealsData, isLoading } = useDeals();
  const deals = (dealsData ?? []).map(mapDealToView);

  return (
    <div className="p-5">
      {isLoading ? (
        <div className="text-center py-8 text-muted-foreground">Chargement...</div>
      ) : (
        <DealsList deals={deals} />
      )}
    </div>
  );
}
