import { useEffect, useMemo, useState, type JSX } from "react";
import type { Deal } from "./types";
import Gallery from "./containers/Gallery";
import ProductDetails from "./containers/ProductDetails";
import PurchaseCard from "./containers/PurchaseCard";
import Reviews from "./containers/Reviews";
import { Card, CardContent } from "@components/ui/card";
import { VStack } from "@/common/components";
import { Heading } from "@/common/containers/Heading";
import DealsList from "@/common/containers/DealList";
import {
  dealService,
  useCommentairesByDeal,
  useDeal,
  useDealsByStatut,
} from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "@/common/components/ui/button";
import { useQueries } from "@tanstack/react-query";
import { useAuth } from "@/common/context/AuthContext";
import { useI18n } from "@/common/hooks/useI18n";
export default function DealDetail(): JSX.Element {
  const { id = "" } = useParams<{ id: string }>();
  const { data: dealData, isLoading, isError } = useDeal(id);
  const { data: commentaires = [] } = useCommentairesByDeal(id);
  const { data: dealsData } = useDealsByStatut(StatutDeal.PUBLIE);
  const { user } = useAuth();
  const similarDeals = (dealsData ?? [])
    .filter((d) => d.uuid !== id)
    .map(mapDealToView);
  const navigate = useNavigate();

  const { t } = useI18n("deals");

  const orderedImagesMeta = useMemo(() => {
    const images = [...(dealData?.listeImages ?? [])];
    return images.sort(
      (a, b) => Number(Boolean(b.isPrincipal)) - Number(Boolean(a.isPrincipal)),
    );
  }, [dealData?.listeImages]);

  const imageUrlQueries = useQueries({
    queries: orderedImagesMeta.map((img) => ({
      queryKey: ["deals", "detail", id, "image-url", img.imageUuid],
      queryFn: () => dealService.getImageUrl(id, img.imageUuid as string),
      enabled: !!id && !!img.imageUuid,
    })),
  });

  const dealImages = useMemo(() => {
    const urls = imageUrlQueries
      .map((q) => q.data?.url)
      .filter((url): url is string => !!url);
    return urls.length > 0 ? urls : ["/placeholder.svg"];
  }, [imageUrlQueries]);

  const commentairesRacine = useMemo(
    () =>
      commentaires.filter((commentaire) => !commentaire.commentaireParentUuid),
    [commentaires],
  );

  const noteMoyenne = useMemo(() => {
    if (!commentairesRacine.length) return 0;
    const total = commentairesRacine.reduce(
      (sum, commentaire) => sum + (Number(commentaire.note) || 0),
      0,
    );
    return Number((total / commentairesRacine.length).toFixed(1));
  }, [commentairesRacine]);

  const deal = useMemo<Deal | null>(() => {
    if (!dealData) return null;

    return {
      id: dealData.uuid,
      title: dealData.titre,
      shortSubtitle: dealData.description,
      priceOriginal: Number(dealData.prixDeal) || 0,
      priceDeal: Number(dealData.prixPart) || 0,
      pricePerPart: Number(dealData.prixPart) || 0,
      rating: noteMoyenne,
      reviewsCount: commentairesRacine.length,
      images: dealImages,
      description: dealData.description,
      highlights: dealData.listePointsForts ?? [],
      location: [dealData.ville, dealData.pays].filter(Boolean).join(", "),
      expiryDate: dealData.dateExpiration,
      partsTotal: Number(dealData.nbParticipants) || 0,
      partsSold: 0,
      minRequired: 1,
      supplier: { name: dealData.createurNom },
    };
  }, [commentairesRacine.length, dealData, dealImages, noteMoyenne]);

  const [qty, setQty] = useState(1);
  const [partsSold, setPartsSold] = useState(0);

  useEffect(() => {
    setPartsSold(deal?.partsSold ?? 0);
  }, [deal?.id, deal?.partsSold]);

  const currentPartsSold = partsSold;
  const currentPartsTotal = deal?.partsTotal ?? 0;
  const currentMinRequired = deal?.minRequired ?? 1;
  const currentPricePerPart = deal?.pricePerPart ?? 0;

  const partsRemaining = currentPartsTotal - currentPartsSold;
  const canBuy = qty <= partsRemaining && qty >= 1;
  const willReachMin = currentPartsSold + qty >= currentMinRequired;
  const activated = currentPartsSold >= currentMinRequired;
  const totalPrice = qty * currentPricePerPart;

  function handleBuy() {
    if (!canBuy || !deal) return;
    setPartsSold((s) => s + qty);
    navigate(`/deals/${deal.id}/checkout`, {
      state: { deal, qty, total: totalPrice },
    });
  }

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        Chargement du deal...
      </div>
    );
  }

  if (isError || !deal) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        Deal introuvable.
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          <main className="flex-1">
            <Gallery images={deal.images} />
            <ProductDetails deal={{ ...deal, partsSold: currentPartsSold }} />
            <Reviews
              dealUuid={deal.id}
              isMerchant={user?.id === dealData?.createurUuid}
            />
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
                  <h4 className="font-semibold">{t("location")}</h4>
                  <p className="text-sm text-gray-600 mt-2">{deal.location}</p>
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
