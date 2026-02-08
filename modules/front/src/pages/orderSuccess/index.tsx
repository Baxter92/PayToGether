import { HStack, VStack } from "@/common/components";
import Grid from "@/common/components/Grid";
import { Button } from "@/common/components/ui/button";
import { Separator } from "@/common/components/ui/separator";
import { PATHS } from "@/common/constants/path";
import { Heading } from "@/common/containers/Heading";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { CheckCircle, Package, Truck, MapPin, ArrowRight } from "lucide-react";
import type { JSX } from "react";

export default function OrderSuccess(): JSX.Element {
  return (
    <main className="min-h-screen bg-background text-foreground">
      {/* Success Container */}
      <div className="max-w-2xl mx-auto px-6 py-16">
        {/* Success Icon & Message */}
        <VStack className="text-center mb-12" align="center">
          <div className="mb-6 p-4 rounded-full bg-green-50 dark:bg-green-950">
            <CheckCircle
              className="w-16 h-16 text-green-600 dark:text-green-400"
              strokeWidth={1.5}
            />
          </div>
          <Heading
            title="Commande confirmée !"
            level={1}
            description="Merci pour votre achat"
            descriptionSize="lg"
          />
          <p className="text-sm text-muted-foreground">
            Numéro de commande :{" "}
            <span className="font-semibold text-foreground">
              #CMD-2024-789456
            </span>
          </p>
        </VStack>

        <Grid gap={10} cols={{ md: 2 }} className="mb-12">
          {/* Order Summary Card */}
          <div className="bg-card rounded-lg border border-border p-8 col-span-2">
            <Heading
              title="Résumé de votre commande"
              level={5}
              className="mb-2"
            />

            <VStack spacing={4} className="mb-8">
              {/* Order Item */}
              <HStack justify="between">
                <Heading
                  title="Produit Premium"
                  level={4}
                  spacing={1}
                  description="Quantité: 2"
                />
                <p className="font-semibold">{formatCurrency(149.98)}</p>
              </HStack>
              <Separator className="mt-2" />

              {/* Totals */}
              <VStack spacing={3} className="pt-4">
                <HStack justify="between" className="text-sm">
                  <span className="text-muted-foreground">Sous-total</span>
                  <span>{formatCurrency(149.98)}</span>
                </HStack>
                <HStack justify="between" className="text-sm">
                  <span className="text-muted-foreground">Livraison</span>
                  <span>{formatCurrency(9.99)}</span>
                </HStack>
                <HStack justify="between" className="text-sm">
                  <span className="text-muted-foreground">Taxes</span>
                  <span>{formatCurrency(31.99)}</span>
                </HStack>
                <Separator className="mt-2" />
                <HStack justify="between" className="text-lg font-bold pt-3">
                  <span>Total</span>
                  <span>{formatCurrency(191.96)}</span>
                </HStack>
              </VStack>
            </VStack>
          </div>

          {/* Shipping Address */}
          <div className="bg-card rounded-lg border border-border p-6">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <Truck className="w-5 h-5 text-muted-foreground" />
              Adresse de livraison
            </h3>
            <p className="text-sm">
              Jean Dupont
              <br />
              42 rue de la Paix
              <br />
              75000 Paris
              <br />
              France
            </p>
          </div>

          {/* Billing Address */}
          <div className="bg-card rounded-lg border border-border p-6">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <MapPin className="w-5 h-5 text-muted-foreground" />
              Adresse de facturation
            </h3>
            <p className="text-sm">
              Jean Dupont
              <br />
              42 rue de la Paix
              <br />
              75000 Paris
              <br />
              France
            </p>
          </div>
        </Grid>

        {/* CTA Buttons */}
        <HStack justify="center" spacing={4}>
          <Button
            title="Suivi de commande"
            to={PATHS.ORDERS}
            leftIcon={<Package className="w-5 h-5" />}
          />
          <Button
            title="Retour à la boutique"
            to={PATHS.HOME}
            variant={"ghost"}
            rightIcon={<ArrowRight className="w-5 h-5" />}
          />
        </HStack>

        {/* Email Confirmation */}
        <div className="mt-12 pt-8 border-t border-border text-center">
          <p className="text-sm text-muted-foreground mb-2">
            Un email de confirmation a été envoyé à{" "}
            <span className="font-semibold text-foreground">
              jean@example.com
            </span>
          </p>
          <p className="text-xs text-muted-foreground">
            Vérifiez votre dossier de courrier indésirable si vous ne le recevez
            pas dans quelques minutes
          </p>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-border mt-16">
        <div className="max-w-4xl mx-auto px-6 py-8 text-center text-sm text-muted-foreground">
          <p>Questions ? Contactez notre support à support@store.com</p>
        </div>
      </footer>
    </main>
  );
}
