import DealCard from "@/components/dealCard";

interface DealsListProps {
  deals: any[];
}

export default function DealsList({ deals }: DealsListProps) {
  if (deals.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-lg text-muted-foreground mb-4">
          Aucun deal ne correspond à vos critères
        </p>
        <p className="text-sm text-muted-foreground">
          Essayez de modifier vos filtres
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {deals.map((deal) => (
        <DealCard key={deal.id} deal={deal} />
      ))}
    </div>
  );
}
