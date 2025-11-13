import { mockDeals, slides } from "@/constants/data";
import Hero from "@/components/Hero";
import Features from "./components/Features";

export default function Home() {
  return (
    <div className="mx-auto">
      <Hero slides={slides} />
      <Features />
    </div>
  );
}
