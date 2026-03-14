import { useI18n } from "@/common/hooks/useI18n";
import { Card, CardContent } from "@components/ui/card";
import { Button } from "@components/ui/button";
import { Separator } from "@components/ui/separator";
import type { Deal } from "../types";
import Counter from "@/common/components/Counter";
import { HStack } from "@/common/components";
import { Share2, Check } from "lucide-react";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { useState } from "react";
import { toast } from "sonner";
type PurchaseCardProps = {
  deal: Deal;
  showAction?: boolean;
  onBuy?: () => void;
  canBuy?: boolean;
  activated?: boolean;
  willReachMin?: boolean;
  qty: number;
  setQty: (qty: number) => void;
  partsRemaining: number;
  totalPrice: number;
};

export default function PurchaseCard({
  deal,
  showAction = true,
  onBuy,
  canBuy,
  activated,
  willReachMin,
  qty,
  setQty,
  partsRemaining,
  totalPrice,
}: PurchaseCardProps) {
  const { t } = useI18n("deals");
  const [isShared, setIsShared] = useState(false);

  const handleShare = async (): Promise<void> => {
    const dealUrl = `${window.location.origin}/deals/${deal.id}`;
    const shareData = {
      title: deal.title,
      text: `Découvrez cette offre: ${deal.title}`,
      url: dealUrl,
    };

    try {
      // Utiliser l'API Web Share si disponible (mobile)
      if (navigator.share) {
        await navigator.share(shareData);
        toast.success(t("shared"));
      } else {
        // Sinon, copier le lien dans le presse-papiers
        await navigator.clipboard.writeText(dealUrl);
        setIsShared(true);
        toast.success(t("linkCopied"));

        // Réinitialiser l'icône après 2 secondes
        setTimeout(() => setIsShared(false), 2000);
      }
    } catch (error) {
      // Ignorer les erreurs (l'utilisateur a annulé le partage)
      if (error instanceof Error && error.name !== "AbortError") {
        console.error("Erreur lors du partage:", error);
        toast.error(t("shareError"));
      }
    }
  };

  return (
    <Card>
      <CardContent>
        <div className="flex items-baseline gap-3">
          <div>
            <div className="text-sm text-gray-500 dark:text-gray-400">
              {t("pricePerPart")}
            </div>
            <div className="text-2xl font-bold text-foreground">
              {formatCurrency(deal.priceDeal ?? 0)}
            </div>
            <div className="text-sm text-gray-500 dark:text-gray-400">
              ({formatCurrency(deal.priceOriginal)})
            </div>
          </div>
        </div>

        <div className="mt-4">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            {t("chooseParts")}
          </div>
          <Counter
            qty={qty}
            setQty={setQty}
            max={partsRemaining}
            min={1}
            className="mt-3"
          />
          <div className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            {t("partsAvailable", { count: partsRemaining - qty })}
          </div>
        </div>

        <div className="mt-4">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            {t("total")}
          </div>
          <div className="text-xl font-semibold text-foreground">
            {formatCurrency(totalPrice)}
          </div>
        </div>

        <div className="mt-4">
          {/* {!activated ? (
            <div className="text-sm text-yellow-700 dark:text-yellow-600">
              En attente : l'offre s'activera si au moins {deal.minRequired}{" "}
              parts sont vendues avant{" "}
              {new Date(deal.expiryDate ?? "").toLocaleDateString()}.
            </div>
          ) : ( */}
          {/* <div className="text-sm text-green-700 dark:text-green-400">
            Offre activée — Vous recevrez votre part.
          </div> */}
          {/* )} */}
        </div>

        {showAction && (
          <Button className="mt-4 w-full" onClick={onBuy} disabled={!canBuy}>
            {canBuy
              ? activated || willReachMin
                ? t("buyNow")
                : t("reserve")
              : t("unavailableQuantity")}
          </Button>
        )}

        <Separator className="my-3" />

        <div className="text-sm text-gray-700 dark:text-gray-300">
          <div className="flex items-center gap-2">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5 text-gray-500 dark:text-gray-400"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path d="M2 5a2 2 0 012-2h2.5a.5.5 0 01.5.5V5h6V3.5a.5.5 0 01.5-.5H16a2 2 0 012 2v2h-2V6H4v9a1 1 0 001 1h9v2H5a3 3 0 01-3-3V5z" />
            </svg>
            <span>{t("deliveryInfo")}</span>
          </div>
        </div>

        <Separator className="my-3" />

        <HStack>
          <Button
            variant="outline"
            leftIcon={isShared ? <Check className="w-4 h-4" /> : <Share2 className="w-4 h-4" />}
            tooltip={t("share")}
            onClick={handleShare}
          >
            {isShared ? t("shared") : t("share")}
          </Button>
          {/* Bouton like - fonctionnalité non implémentée pour le moment */}
          {/* <Button
            variant="outline"
            leftIcon={<Heart className="w-4 h-4 fill-current text-red-600" />}
            tooltip={t("addToFavorites")}
          >
            {t("addToFavorites")}
          </Button> */}
        </HStack>
      </CardContent>
    </Card>
  );
}
