import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { type JSX } from "react";

export default function MyPurchases(): JSX.Element {
  return (
    <section>
      <Heading
        level={2}
        title="Mes achats"
        description="Liste de tes bons / vouchers achetés avec actions rapides."
        underline
      />

      <DealsList deals={mockDeals} viewMode="list" showFilters={false} />
    </section>
  );
}
