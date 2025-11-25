import { mockDeals, slides } from "@/common/constants/data";

import Features from "./containers/Features";
import Hero from "@containers/Hero";
import type { JSX } from "react";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { VStack } from "@/common/components";

export default function Home(): JSX.Element {
  return (
    <div className="mx-auto">
      <Hero slides={slides} />
      <Features />
      <VStack spacing={10} className="py-8 max-w-7xl mx-auto px-4">
        <Heading
          title="Offres Promotionnelles"
          level={2}
          spacing={16}
          underline
          underlineStyle="bar"
        />
        <DealsList
          deals={mockDeals}
          showFilters={false}
          showPagination={false}
          itemsPerPage={4}
        />

        <Heading
          title="Populaire sur PayToGether"
          level={2}
          spacing={16}
          underline
          underlineStyle="bar"
        />
        <DealsList
          deals={mockDeals}
          showFilters={false}
          showPagination={false}
          itemsPerPage={4}
        />

        <Heading
          title="Tellement d'économies... Parcourez-les toutes !"
          level={3}
          spacing={16}
          underline
          underlineStyle="bar"
        />
        <DealsList deals={mockDeals} showFilters={false} />
      </VStack>
    </div>
  );
}
