import type { CheckoutState } from "../types";
import { Card, CardContent } from "@/common/components/ui/card";
import { HStack, VStack } from "@/common/components";
import { formatCurrency } from "@/common/utils/formatCurrency";
import type { JSX } from "react";
import { Separator } from "@/common/components/ui/separator";

interface OrderSummaryProps {
  deal?: CheckoutState["deal"];
  qty: number;
  subtotal: number;
  deliveryFee: number;
  total: number;
}

export default function OrderSummary({
  deal,
  qty,
  subtotal,
  deliveryFee,
  total,
}: OrderSummaryProps): JSX.Element {
  return (
    <Card className="border-border bg-card shadow-sm">
      <CardContent className="pt-6">
        <h3 className="text-lg font-semibold text-foreground mb-4">
          Récapitulatif
        </h3>

        <VStack spacing={4} className="mb-4">
          <div className="flex items-start gap-3 p-3 bg-secondary rounded-lg">
            <div className="flex-1">
              <p className="font-medium text-foreground text-sm">
                {deal?.title ?? "Part de produit"}
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Quantité : {qty} × {formatCurrency(deal?.pricePerPart || 0)}
              </p>
              {deal?.partWeightKg && (
                <p className="text-xs text-muted-foreground">
                  Poids total : {(qty * deal.partWeightKg).toFixed(2)} kg
                </p>
              )}
            </div>
            <div className="font-semibold text-primary">
              {formatCurrency(subtotal)}
            </div>
          </div>
        </VStack>

        <Separator className="my-4" />

        <VStack spacing={3} className="mb-4">
          <HStack justify="between" className="text-sm">
            <span className="text-muted-foreground">Sous-total</span>
            <span className="font-medium text-foreground">
              {formatCurrency(subtotal)}
            </span>
          </HStack>
          <HStack justify="between" className="text-sm">
            <span className="text-muted-foreground">Livraison</span>
            <span className="font-medium text-foreground">
              {deliveryFee > 0 ? formatCurrency(deliveryFee) : "Gratuit"}
            </span>
          </HStack>
        </VStack>

        <Separator className="my-4" />

        <HStack justify="between" className="mb-4">
          <span className="font-semibold text-foreground">Total à payer</span>
          <span className="text-2xl font-bold text-primary">
            {formatCurrency(total)}
          </span>
        </HStack>

        <p className="text-xs text-muted-foreground">
          ✓ Remboursement possible si l'offre n'est pas activée
        </p>
      </CardContent>
    </Card>
  );
}
