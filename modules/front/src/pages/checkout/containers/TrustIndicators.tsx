import { useI18n } from "@hooks/useI18n";
import { HStack, VStack } from "@/common/components";
import { Card, CardContent } from "@/common/components/ui/card";
import { CheckIcon, CreditCardIcon, TruckIcon } from "lucide-react";
import type { JSX } from "react";

export default function TrustIndicators(): JSX.Element {
  const { t } = useI18n("checkout");

  return (
    <Card className="border-border bg-accent/5 shadow-sm">
      <CardContent className="pt-6">
        <VStack spacing={3}>
          <HStack spacing={2}>
            <CheckIcon className="w-5 h-5 text-accent flex-shrink-0" />
            <span className="text-sm text-foreground">
              {t("checkout.securePayment")}
            </span>
          </HStack>
          <HStack spacing={2}>
            <TruckIcon className="w-5 h-5 text-accent flex-shrink-0" />
            <span className="text-sm text-foreground">
              {t("checkout.fastDelivery")}
            </span>
          </HStack>
          <HStack spacing={2}>
            <CreditCardIcon className="w-5 h-5 text-accent flex-shrink-0" />
            <span className="text-sm text-foreground">
              {t("checkout.multiplePaymentMethods")}
            </span>
          </HStack>
        </VStack>
      </CardContent>
    </Card>
  );
}
