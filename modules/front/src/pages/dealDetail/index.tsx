import { useState, type JSX } from "react";
import type { Deal } from "./types";
import Gallery from "./containers/Gallery";
import ProductDetails from "./containers/ProductDetails";
import PurchaseCard from "./containers/PurchaseCard";
import Reviews from "./containers/Reviews";
import { Card, CardContent } from "@components/ui/card";
import { VStack } from "@/common/components";
import { Heading } from "@/common/containers/Heading";
import DealsList from "@/common/containers/DealList";
import { useDealsByStatut } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import { useNavigate } from "react-router-dom";
import { Button } from "@/common/components/ui/button";

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
    "/images/FAMILOV_1762258046.jpeg",
    "/images/images.jpg",
    "/images/filet-de-boeuf-entier-15-25kg.jpg"
  ],
  description:
    "Achetez des parts individuelles (0.5 kg) d'une caisse de bœuf premium. Chaque part est emballée sous vide, prête à être conservée au réfrigérateur ou congelée. L'offre s'active uniquement si suffisamment de parts sont vendues avant la date d'expiration.",
  highlights: [
    "Part 0.5 kg",
    "Élevage local certifié",
    "Emballage sous vide",
    "Livraison réfrigérée"
  ],
  whatsIncluded: ["Part 0.5 kg emballée sous vide"],
  location: "Douala, Cameroon (livraison disponible dans les grandes villes)",
  expiryDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 10).toISOString(),
  partsTotal: 50,
  partsSold: 19,
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

export default function DealDetail({
  deal = mockDeal,
}: {
  deal?: Deal;
}): JSX.Element {
  const { data: dealsData } = useDealsByStatut(StatutDeal.PUBLIE);
  const similarDeals = (dealsData ?? []).map(mapDealToView);
  const navigate = useNavigate();

  const [qty, setQty] = useState(1);
  const [partsSold, setPartsSold] = useState(deal.partsSold);

  const partsRemaining = deal.partsTotal - partsSold;
  const canBuy = qty <= partsRemaining && qty >= 1;
  const willReachMin = partsSold + qty >= deal.minRequired;
  const activated = partsSold >= deal.minRequired;
  const totalPrice = qty * deal.pricePerPart;

  function handleBuy() {
    if (!canBuy) return;
    setPartsSold((s) => s + qty);
    navigate(`/deals/${deal.id}/checkout`, {
      state: { deal, qty, total: totalPrice },
    });
  }
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
                <PurchaseCard
                  deal={deal}
                  onBuy={handleBuy}
                  qty={qty}
                  setQty={setQty}
                  partsRemaining={partsRemaining}
                  totalPrice={totalPrice}
                  canBuy={canBuy}
                  activated={activated}
                  willReachMin={willReachMin}
                />
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
            deals={similarDeals}
            showFilters={false}
            itemsPerPage={4}
            showPagination={false}
          />
        </VStack>
      </div>
      <div className="fixed bottom-0 left-0 right-0 z-50 bg-white border-t p-4 sm:hidden">
        <Button
          className="w-full h-12 text-base"
          onClick={handleBuy}
          disabled={!canBuy}
        >
          {canBuy
            ? activated || willReachMin
              ? "Acheter maintenant"
              : "Réserver (en attente d'activation)"
            : "Quantité non disponible"}
        </Button>
      </div>
    </div>
  );
}
