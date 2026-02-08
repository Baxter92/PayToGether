import type { CheckoutState } from "../types";
import { useI18n } from "@hooks/useI18n";
import { Card, CardContent } from "@/common/components/ui/card";
import { HStack, VStack } from "@/common/components";
import { useFormattedCurrency } from "@/common/hooks/useI18n";
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
  const { t } = useI18n("checkout");
  const formatCurrency = useFormattedCurrency();

  return (
    <Card className="border-border bg-card shadow-sm">
      <CardContent className="pt-6">
        <h3 className="text-lg font-semibold text-foreground mb-4">
          {t("checkout.summary")}
        </h3>

        <VStack spacing={4} className="mb-4">
          <div className="flex items-start gap-3 p-3 bg-secondary rounded-lg">
            <div className="flex-1">
              <p className="font-medium text-foreground text-sm">
                {deal?.title ?? t("checkout.productPart")}
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                {t("checkout.quantity")} {qty} ×{" "}
                {formatCurrency(deal?.pricePerPart || 0)}
              </p>
              {deal?.partWeightKg && (
                <p className="text-xs text-muted-foreground">
                  {t("checkout.totalWeight")}{" "}
                  {(qty * deal.partWeightKg).toFixed(2)} kg
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
            <span className="text-muted-foreground">
              {t("orderSuccess.subtotal")}
            </span>
            <span className="font-medium text-foreground">
              {formatCurrency(subtotal)}
            </span>
          </HStack>
          <HStack justify="between" className="text-sm">
            <span className="text-muted-foreground">
              {t("orderSuccess.delivery")}
            </span>
            <span className="font-medium text-foreground">
              {deliveryFee > 0
                ? formatCurrency(deliveryFee)
                : t("checkout.free")}
            </span>
          </HStack>
        </VStack>

        <Separator className="my-4" />

        <HStack justify="between" className="mb-4">
          <span className="font-semibold text-foreground">
            {t("checkout.totalToPay")}
          </span>
          <span className="text-2xl font-bold text-primary">
            {formatCurrency(total)}
          </span>
        </HStack>

        <p className="text-xs text-muted-foreground">
          ✓ {t("checkout.refundNotice")}
        </p>
      </CardContent>
    </Card>
  );
}
