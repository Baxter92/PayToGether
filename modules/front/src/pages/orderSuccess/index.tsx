import { HStack, VStack } from "@/common/components";
import Grid from "@/common/components/Grid";
import { Button } from "@/common/components/ui/button";
import { Separator } from "@/common/components/ui/separator";
import { PATHS } from "@/common/constants/path";
import { Heading } from "@/common/containers/Heading";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { CheckCircle, Package, Truck, MapPin, ArrowRight } from "lucide-react";
import type { JSX } from "react";
import { useLocation, useParams } from "react-router-dom";
import { useI18n } from "@/common/hooks/useI18n";
import { useOrder } from "@/common/api/hooks/useOrders";
import { useAuth } from "@/common/context/AuthContext";

type OrderSuccessState = {
  orderId?: string;
  deal?: {
    title?: string;
    pricePerPart?: number;
  };
  qty?: number;
  total?: number;
  shipping?: {
    fullName?: string;
    address?: string;
    postalCode?: string;
    city?: string;
    complementAddress?: string;
  };
  delivery?: {
    deliveryMethod?: "home" | "pickup";
  };
};

export default function OrderSuccess(): JSX.Element {
  const { t } = useI18n("orderSuccess");
  const { user } = useAuth();
  const location = useLocation();
  const { id: routeId } = useParams();
  const state = location.state as OrderSuccessState | null;
  const orderId = state?.orderId ?? routeId ?? "";

  const { data: orderData } = useOrder(orderId);

  const orderNumber = orderId || orderData?.uuid;
  const dealTitle = state?.deal?.title ?? orderData?.dealTitre;
  const unitPrice = state?.deal?.pricePerPart ?? orderData?.dealPrixPart;
  const quantity =
    typeof state?.qty === "number"
      ? state?.qty
      : typeof unitPrice === "number" &&
          unitPrice > 0 &&
          typeof orderData?.montantTotal === "number"
        ? Math.max(1, Math.round(orderData.montantTotal / unitPrice))
        : undefined;
  const subtotal =
    typeof unitPrice === "number" && typeof quantity === "number"
      ? unitPrice * quantity
      : typeof orderData?.montantTotal === "number"
        ? orderData.montantTotal
        : typeof state?.total === "number"
          ? state.total
          : undefined;
  const total =
    typeof state?.total === "number"
      ? state.total
      : typeof orderData?.montantTotal === "number"
        ? orderData.montantTotal
        : subtotal;
  const inferredDeliveryFee =
    typeof total === "number" && typeof subtotal === "number"
      ? Math.max(0, total - subtotal)
      : undefined;
  const deliveryFee = state?.delivery ? inferredDeliveryFee : undefined;

  const formatMoney = (value?: number) =>
    typeof value === "number" ? formatCurrency(value) : t("notAvailable");

  const buildAddressLines = (input?: OrderSuccessState["shipping"]) => {
    const lines: string[] = [];
    if (input?.fullName) lines.push(input.fullName);
    if (input?.address) lines.push(input.address);
    if (input?.complementAddress) lines.push(input.complementAddress);
    const cityLine = [input?.postalCode, input?.city]
      .filter(Boolean)
      .join(" ");
    if (cityLine) lines.push(cityLine);
    return lines;
  };

  const shippingLines = buildAddressLines(state?.shipping);
  const billingLines = shippingLines;
  const quantityText = `${t("quantity")} ${
    typeof quantity === "number" ? quantity : t("notAvailable")
  }`;

  const renderAddress = (lines: string[]) => {
    if (lines.length === 0) {
      return (
        <p className="text-sm text-muted-foreground">{t("notAvailable")}</p>
      );
    }

    return (
      <p className="text-sm">
        {lines.map((line, index) => (
          <span key={`${line}-${index}`}>
            {line}
            {index < lines.length - 1 && <br />}
          </span>
        ))}
      </p>
    );
  };

  return (
    <main className="min-h-screen bg-background text-foreground">
      <div className="max-w-2xl mx-auto px-6 py-16">
        <VStack className="text-center mb-12" align="center">
          <div className="mb-6 p-4 rounded-full bg-green-50 dark:bg-green-950">
            <CheckCircle
              className="w-16 h-16 text-green-600 dark:text-green-400"
              strokeWidth={1.5}
            />
          </div>
          <Heading
            title={t("title")}
            level={1}
            description={t("subtitle")}
            descriptionSize="lg"
          />
          <p className="text-sm text-muted-foreground">
            {t("orderNumber")} {" "}
            <span className="font-semibold text-foreground">
              {orderNumber || t("notAvailable")}
            </span>
          </p>
        </VStack>

        <Grid gap={10} cols={{ md: 2 }} className="mb-12">
          <div className="bg-card rounded-lg border border-border p-8 col-span-2">
            <Heading title={t("orderSummary")} level={5} className="mb-2" />

            <VStack spacing={4} className="mb-8">
              <HStack justify="between">
                <Heading
                  title={dealTitle || t("notAvailable")}
                  level={4}
                  spacing={1}
                  description={quantityText}
                />
                <p className="font-semibold">{formatMoney(subtotal)}</p>
              </HStack>
              <Separator className="mt-2" />

              <VStack spacing={3} className="pt-4">
                <HStack justify="between" className="text-sm">
                  <span className="text-muted-foreground">
                    {t("subtotal")}
                  </span>
                  <span>{formatMoney(subtotal)}</span>
                </HStack>
                <HStack justify="between" className="text-sm">
                  <span className="text-muted-foreground">
                    {t("delivery")}
                  </span>
                  <span>{formatMoney(deliveryFee)}</span>
                </HStack>
                <HStack justify="between" className="text-sm">
                  <span className="text-muted-foreground">{t("taxes")}</span>
                  <span>{t("notAvailable")}</span>
                </HStack>
                <Separator className="mt-2" />
                <HStack justify="between" className="text-lg font-bold pt-3">
                  <span>{t("total")}</span>
                  <span>{formatMoney(total)}</span>
                </HStack>
              </VStack>
            </VStack>
          </div>

          <div className="bg-card rounded-lg border border-border p-6">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <Truck className="w-5 h-5 text-muted-foreground" />
              {t("shippingAddress")}
            </h3>
            {renderAddress(shippingLines)}
          </div>

          <div className="bg-card rounded-lg border border-border p-6">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <MapPin className="w-5 h-5 text-muted-foreground" />
              {t("billingAddress")}
            </h3>
            {renderAddress(billingLines)}
          </div>
        </Grid>

        <HStack justify="center" spacing={4}>
          <Button
            title={t("trackOrder")}
            to={PATHS.ORDERS}
            leftIcon={<Package className="w-5 h-5" />}
          />
          <Button
            title={t("backToShop")}
            to={PATHS.HOME}
            variant={"ghost"}
            rightIcon={<ArrowRight className="w-5 h-5" />}
          />
        </HStack>

        {user?.email && (
          <div className="mt-12 pt-8 border-t border-border text-center">
            <p className="text-sm text-muted-foreground mb-2">
              {t("emailConfirmation", { email: user.email })}
            </p>
            <p className="text-xs text-muted-foreground">{t("checkSpam")}</p>
          </div>
        )}
      </div>

      <footer className="border-t border-border mt-16">
        <div className="max-w-4xl mx-auto px-6 py-8 text-center text-sm text-muted-foreground">
          <p>{t("questions")}</p>
        </div>
      </footer>
    </main>
  );
}
