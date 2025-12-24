import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import React from "react";

export default function Deals() {
  return (
    <div className="p-5">
      <DealsList deals={mockDeals} />
    </div>
  );
}
