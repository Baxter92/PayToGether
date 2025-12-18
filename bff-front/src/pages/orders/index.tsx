import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import type { JSX } from "react";

export default function Orders(): JSX.Element {
  return (
    <main className="bg-white">
      <div className="max-w-7xl mx-auto py-4">
        <Heading
          title="Mes commandes"
          description="Historique de vos achats"
          level={2}
          underline
        />
        <DealsList
          deals={mockDeals}
          cols={{ md: 2, base: 1, lg: 3 }}
          viewMode="list"
          showFilters={false}
          tableProps={{
            enableSelection: false,
          }}
        />
      </div>
    </main>
  );
}
