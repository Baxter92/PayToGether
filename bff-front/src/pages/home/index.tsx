import { mockDeals } from "@/constants/data";
import { useState } from "react";
import DealsFilter from "./components/DealsFilter";
import DealsList from "./components/DealsList";
import Features from "./components/Features";
import HStack from "@/components/hstack";
import VStack from "@/components/vstack";

export default function Home() {
  const [filters, setFilters] = useState({
    category: "all",
    priceRange: [0, 300],
    city: "all",
    status: "all",
  });

  const filteredDeals = mockDeals.filter((deal) => {
    const categoryMatch =
      filters.category === "all" || deal.category === filters.category;
    const priceMatch =
      deal.groupPrice >= filters.priceRange[0] &&
      deal.groupPrice <= filters.priceRange[1];
    const cityMatch = filters.city === "all" || deal.city === filters.city;

    let statusMatch = true;
    if (filters.status === "active") {
      statusMatch = deal.sold < deal.total;
    } else if (filters.status === "sold-out") {
      statusMatch = deal.sold >= deal.total;
    }

    return categoryMatch && priceMatch && cityMatch && statusMatch;
  });
  return (
    <div className="mx-auto">
      <Features />
      <VStack spacing={300}>
        <div>te</div>
        <div>uuyuy</div>
      </VStack>
    </div>
  );
}
