import React from "react";
import type { Deal } from "./types";
import Gallery from "./containers/Gallery";
import ProductDetails from "./containers/ProductDetails";
import PurchaseCard from "./containers/PurchaseCard";
import Reviews from "./containers/Reviews";
import { Card, CardContent } from "@components/ui/card";
import { Button } from "@components/ui/button";
import { VStack } from "@/common/components";
import { Heading } from "@/common/containers/Heading";
import DealsList from "@/common/containers/DealList";
import { mockDeals } from "@/common/constants/data";

const mockDeal: Deal = {
  id: "deal-003",
  title: "Parts de Boeuf Premium — Part (0.5 kg)",
  shortSubtitle:
    "Parts individuelles de viande de bœuf élevées localement — livraison réfrigérée",
  priceOriginal: 80,
  priceDeal: 59,
  pricePerPart: 5.9,
  savingsText: "-26%",
  rating: 4.8,
  reviewsCount: 342,
  images: [
    "https://images.unsplash.com/photo-1551782450-a2132b4ba21d?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=beef1",
    "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop&ixlib=rb-4.0.3&s=beef2",
    "https://images.unsplash.com/photo-1551218808-94e220e084d2?q=80&w=1200&auto=format&fit=crop&ixlib=rb-4.0.3&s=beef3",
  ],
  description:
    "Achetez des parts individuelles (0.5 kg) d'une caisse de bœuf premium. Chaque part est emballée sous vide, prête à être conservée au réfrigérateur ou congelée. L'offre s'active uniquement si suffisamment de parts sont vendues avant la date d'expiration.",
  highlights: [
    "Part 0.5 kg",
    "Élevage local certifié",
    "Emballage sous vide",
    "Livraison réfrigérée",
  ],
  whatsIncluded: ["Part 0.5 kg emballée sous vide"],
  location: "Douala, Cameroon (livraison disponible dans les grandes villes)",
  expiryDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 10).toISOString(),
  partsTotal: 50,
  partsSold: 18,
  minRequired: 20,
  partWeightKg: 0.5,
  supplier: { name: "Ferme" },
  packaging: { method: "Sous vide" },
  nutrition: {
    per100g: { calories: 250, protein: "26g", fat: "17g", iron: "2.6mg" },
  },
  cookingTips: ["Saisir les steaks à feu vif 2-3 min par côté"],
  shelfLifeDays: 5,
};

export default function DealDetail({ deal = mockDeal }: { deal?: Deal }) {
  return (
    <div className=" bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          <main className="flex-1">
            <Gallery images={deal.images} />
            <ProductDetails deal={deal} />
            <Reviews count={deal.reviewsCount ?? 0} />
          </main>

          <aside className="w-full lg:w-[360px]">
            <div className="sticky top-20 space-y-4">
              <div>
                <PurchaseCard deal={deal} />
              </div>

              <Card>
                <CardContent>
                  <h4 className="font-semibold">Lieu</h4>
                  <p className="text-sm text-gray-600 mt-2">{deal.location}</p>
                  <div className="mt-3 h-32 bg-gray-100 rounded flex items-center justify-center text-sm text-gray-400">
                    Carte (placeholder)
                  </div>
                </CardContent>
              </Card>
            </div>
          </aside>
        </div>
        <VStack>
          <Heading
            title="Produits similaires"
            level={3}
            underline
            className="mt-8"
          />
          <DealsList
            deals={mockDeals}
            showFilters={false}
            itemsPerPage={4}
            showPagination={false}
          />
        </VStack>
      </div>
    </div>
  );
}
