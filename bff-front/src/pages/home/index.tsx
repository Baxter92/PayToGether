import { mockDeals, slides } from "@/common/constants/data";

import Features from "./containers/Features";
import Hero from "@containers/Hero";
import type { JSX } from "react";
import DealsList from "@/common/containers/DealList";

export default function Home(): JSX.Element {
  return (
    <div className="mx-auto">
      <Hero slides={slides} />
      <Features />
      <DealsList deals={mockDeals} />
    </div>
  );
}
