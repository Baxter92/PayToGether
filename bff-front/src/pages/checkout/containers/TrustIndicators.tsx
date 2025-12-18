import { HStack, VStack } from "@/common/components";
import { Card, CardContent } from "@/common/components/ui/card";
import { CheckIcon, CreditCardIcon, TruckIcon } from "lucide-react";
import type { JSX } from "react";

export default function TrustIndicators(): JSX.Element {
  return (
    <Card className="border-border bg-accent/5 shadow-sm">
      <CardContent className="pt-6">
        <VStack spacing={3}>
          <HStack spacing={2}>
            <CheckIcon className="w-5 h-5 text-accent flex-shrink-0" />
            <span className="text-sm text-foreground">Paiement sécurisé</span>
          </HStack>
          <HStack spacing={2}>
            <TruckIcon className="w-5 h-5 text-accent flex-shrink-0" />
            <span className="text-sm text-foreground">Livraison rapide</span>
          </HStack>
          <HStack spacing={2}>
            <CreditCardIcon className="w-5 h-5 text-accent flex-shrink-0" />
            <span className="text-sm text-foreground">
              Plusieurs moyens de paiement
            </span>
          </HStack>
        </VStack>
      </CardContent>
    </Card>
  );
}
