import { categories, mockDeals, slides } from "@/common/constants/data";

import Features from "./containers/Features";
import Hero from "@containers/Hero";
import type { JSX } from "react";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { VStack } from "@/common/components";
import CategoriesList from "@/common/containers/CategoriesList";
import { Button } from "@/common/components/ui/button";
import { ArrowRight, Sparkles } from "lucide-react";

export default function Home(): JSX.Element {
  return (
    <div className="mx-auto">
      <Hero slides={slides} />
      <Features />
      
      {/* Promotional Deals Section */}
      <section className="py-16 bg-background">
        <VStack spacing={10} className="max-w-7xl mx-auto px-4">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 w-full">
            <Heading
              title="Offres Promotionnelles"
              level={2}
              spacing={8}
              description="Les meilleures réductions du moment"
              underline
              underlineStyle="bar"
              underlineWidth="w-16"
            />
            <Button variant="ghost" className="text-primary hover:text-primary-600 font-semibold gap-2 group">
              Voir toutes les offres
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>
          <DealsList
            deals={mockDeals}
            showFilters={false}
            showPagination={false}
            itemsPerPage={4}
          />
        </VStack>
      </section>

      {/* Popular Section */}
      <section className="py-16 bg-gradient-to-b from-muted/50 to-background relative">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,oklch(0.75_0.15_230_/_0.05),transparent_50%)]" />
        <VStack spacing={10} className="max-w-7xl mx-auto px-4 relative z-10">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 w-full">
            <div className="flex items-start gap-3">
              <div className="p-2 bg-accent/10 rounded-xl">
                <Sparkles className="w-6 h-6 text-accent" />
              </div>
              <Heading
                title="Populaire sur PayToGether"
                level={2}
                spacing={8}
                description="Ce que nos clients adorent"
                underline
                underlineStyle="bar"
                underlineWidth="w-16"
              />
            </div>
            <Button variant="ghost" className="text-primary hover:text-primary-600 font-semibold gap-2 group">
              Explorer tout
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>
          <DealsList
            deals={mockDeals}
            showFilters={false}
            showPagination={false}
            itemsPerPage={4}
          />
        </VStack>
      </section>

      {/* Categories Section */}
      <section className="py-16 bg-background">
        <VStack spacing={10} className="max-w-7xl mx-auto px-4">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 w-full">
            <Heading
              title="Meilleures Catégories"
              level={2}
              spacing={8}
              description="Explorez par domaine d'intérêt"
              underline
              underlineStyle="bar"
              underlineWidth="w-16"
            />
            <Button variant="ghost" className="text-primary hover:text-primary-600 font-semibold gap-2 group">
              Toutes les catégories
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>
          <CategoriesList
            categories={categories}
            showPagination={false}
            showTitle={false}
            cols={{ base: 1, md: 2 }}
            itemsPerPage={4}
          />
        </VStack>
      </section>

      {/* All Deals Section */}
      <section className="py-16 bg-gradient-to-b from-muted/30 to-background">
        <VStack spacing={10} className="max-w-7xl mx-auto px-4">
          <div className="text-center max-w-2xl mx-auto">
            <Heading
              title="Tellement d'économies..."
              level={2}
              spacing={8}
              description="Parcourez toutes nos offres et trouvez celle qui vous convient"
              align="center"
              underline
              underlineStyle="bar"
              underlineWidth="w-20"
            />
          </div>
          <DealsList deals={mockDeals} showFilters={false} />
        </VStack>
      </section>
    </div>
  );
}
