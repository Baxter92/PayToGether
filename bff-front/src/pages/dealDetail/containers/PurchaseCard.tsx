import { useState } from "react";
import { Card, CardContent } from "@components/ui/card";
import { Button } from "@components/ui/button";
import { Separator } from "@components/ui/separator";
import type { Deal } from "../types";
import Counter from "@/common/components/Counter";
import { HStack } from "@/common/components";
import { Heart, Share } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function PurchaseCard({ deal }: { deal: Deal }) {
  const [qty, setQty] = useState(1);
  const [partsSold, setPartsSold] = useState(deal.partsSold);

  const navigate = useNavigate();

  const partsRemaining = deal.partsTotal - partsSold;
  const canBuy = qty <= partsRemaining && qty >= 1;
  const willReachMin = partsSold + qty >= deal.minRequired;
  const activated = partsSold >= deal.minRequired;
  const totalPrice = qty * deal.pricePerPart;

  function handleAdd() {
    if (!canBuy) return;
    setPartsSold((s) => s + qty);
    navigate(`/deals/${deal.id}/checkout`, {
      state: { deal, qty, total: totalPrice },
    });
  }

  return (
    <Card>
      <CardContent>
        <div className="flex items-baseline gap-3">
          <div>
            <div className="text-sm text-gray-500">Prix par part</div>
            <div className="text-2xl font-bold">{deal.pricePerPart}</div>
            <div className="text-sm text-gray-500">
              (~{deal.partWeightKg} kg/part)
            </div>
          </div>

          <div className="ml-auto text-sm text-green-600 font-semibold">
            {deal.savingsText}
          </div>
        </div>

        <div className="mt-4">
          <div className="text-sm text-gray-600">
            Choisir le nombre de parts
          </div>
          <Counter
            qty={qty}
            setQty={setQty}
            max={partsRemaining}
            min={1}
            className="mt-3"
          />
          <div className="mt-2 text-sm text-gray-600">
            {partsRemaining} part(s) disponibles
          </div>
        </div>

        <div className="mt-4">
          <div className="text-sm text-gray-600">Total</div>
          <div className="text-xl font-semibold">{totalPrice}</div>
        </div>

        <div className="mt-4">
          {!activated ? (
            <div className="text-sm text-yellow-700">
              En attente : l'offre s'activera si au moins {deal.minRequired}{" "}
              parts sont vendues avant{" "}
              {new Date(deal.expiryDate ?? "").toLocaleDateString()}.
            </div>
          ) : (
            <div className="text-sm text-green-700">
              Offre activée — Vous recevrez votre part.
            </div>
          )}
        </div>

        <Button className="mt-4 w-full" onClick={handleAdd} disabled={!canBuy}>
          {canBuy
            ? activated || willReachMin
              ? "Acheter maintenant"
              : "Réserver (en attente d'activation)"
            : "Quantité non disponible"}
        </Button>

        <Separator className="my-3" />

        <div className="text-sm text-gray-700">
          <div className="flex items-center gap-2">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5 text-gray-500"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path d="M2 5a2 2 0 012-2h2.5a.5.5 0 01.5.5V5h6V3.5a.5.5 0 01.5-.5H16a2 2 0 012 2v2h-2V6H4v9a1 1 0 001 1h9v2H5a3 3 0 01-3-3V5z" />
            </svg>
            <span>
              Livraison réfrigérée disponible • Remboursement si l'offre n'est
              pas activée
            </span>
          </div>
        </div>

        <Separator className="my-3" />

        <HStack>
          <Button
            leftIcon={<Share className="w-4 h-4" />}
            tooltip={"Partager"}
          />
          <Button
            variant="outline"
            leftIcon={<Heart className="w-4 h-4 fill-current text-red-600" />}
          >
            Mettre en favoris
          </Button>
        </HStack>
      </CardContent>
    </Card>
  );
}
