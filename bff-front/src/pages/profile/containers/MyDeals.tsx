import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { type JSX } from "react";

export default function MyDeals(): JSX.Element {
  return (
    <section>
      <Heading
        level={2}
        title="Toutes mes offres"
        description="Parcourez les deals que vous avez créés"
        underline
      />

      <DealsList
        deals={mockDeals}
        cols={{ md: 2, base: 1, lg: 3 }}
        filterPosition="top"
        viewMode="list"
      />
    </section>
  );
}
