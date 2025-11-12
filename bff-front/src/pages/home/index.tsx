import { mockDeals } from "@/constants/data";
import DealsList from "./components/DealsList";
import Hero from "@/components/hero";
import Features from "./components/Features";

export default function Home() {
  return (
    <div className="mx-auto">
      <Hero />
      <Features />
    </div>
  );
}
