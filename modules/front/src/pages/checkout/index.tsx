import type { JSX } from "react";
import React, { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useI18n } from "@hooks/useI18n";
import { useAuth } from "@/common/context/AuthContext";
import { Card, CardContent } from "@components/ui/card";
import VStack from "@components/VStack";
import type { CheckoutState, DeliveryData, ShippingData } from "./types";
import CheckoutStep from "./components/CheckoutStep";
import ShippingForm from "./containers/ShippingForm";
import { DeliveryForm } from "./containers/DeliveryForm";
import SquarePaymentForm from "./containers/SquarePaymentForm";
import OrderSummary from "./containers/OrderSummary";
import HelpSection from "./containers/HelpSection";
import TrustIndicators from "./containers/TrustIndicators";

export default function CheckoutPage(): JSX.Element {
  const { t } = useI18n("checkout");
  const { state } = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const checkoutState = useMemo(() => (state ?? {}) as CheckoutState, [state]);

  const qty =
    checkoutState.qty && checkoutState.qty > 0 ? checkoutState.qty : 1;
  const deal = checkoutState.deal;
  const pricePerPart = deal?.pricePerPart ?? 0;

  const subtotal = useMemo(() => qty * pricePerPart, [qty, pricePerPart]);

  // Frais de service : 5% du sous-total
  const serviceFees = useMemo(() => subtotal * 0.05, [subtotal]);

  const defaultDeliveryFee = 15;

  // Taxes : 5% sur (sous-total + frais de service)
  const tax = useMemo(() => (subtotal + serviceFees) * 0.05, [subtotal, serviceFees]);

  const [currentStep, setCurrentStep] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const [shippingData, setShippingData] = useState<ShippingData | null>(null);
  const [deliveryData, setDeliveryData] = useState<DeliveryData | null>(null);

  const deliveryFee = useMemo(
    () => (deliveryData?.deliveryMethod === "home" ? defaultDeliveryFee : 0),
    [deliveryData, defaultDeliveryFee],
  );
  const total = useMemo(
    () => subtotal + serviceFees + tax + deliveryFee,
    [subtotal, serviceFees, tax, deliveryFee],
  );

  React.useEffect(() => {
    if (!checkoutState.deal && !checkoutState.dealId) {
      navigate("/deals", { replace: true });
    }
  }, [checkoutState, navigate]);

  const handleShippingSubmit = async (data: ShippingData) => {
    setShippingData(data);
    setCurrentStep(1);
    setApiError(null);
  };

  const handleDeliverySubmit = async (data: DeliveryData) => {
    setDeliveryData(data);
    setCurrentStep(2);
    setApiError(null);
  };

  const handleSquarePaymentSuccess = (paymentId: string): void => {
    const orderId = `ORD-${Date.now()}`;
    navigate(`/orders/${orderId}`, {
      replace: true,
      state: {
        orderId,
        deal,
        qty,
        total,
        shipping: shippingData,
        delivery: deliveryData,
        payment: {
          paymentMethod: "square",
          squarePaymentId: paymentId,
        },
      },
    });
  };

  const handleSquarePaymentError = (error: string): void => {
    setApiError(error);
    setIsSubmitting(false);
  };

  const steps = [
    {
      title: t("shippingTitle"),
      description: t("shippingDescription"),
    },
    {
      title: t("deliveryTitle"),
      description: t("deliveryDescription"),
    },
    {
      title: t("paymentTitle"),
      description: t("paymentDescription"),
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-background to-secondary">
      {/* Header */}
      <div className="border-b border-border bg-card">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-foreground">{t("title")}</h1>
          <p className="text-muted-foreground mt-1">{t("subtitle")}</p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Form */}
          <div className="lg:col-span-2">
            <Card className="border-border bg-card shadow-sm">
              <CardContent className="pt-8">
                <VStack spacing={8}>
                  {/* Step 1: Shipping */}
                  <CheckoutStep
                    stepNumber={1}
                    title={steps[0].title}
                    description={steps[0].description}
                    isActive={currentStep === 0}
                    isCompleted={currentStep > 0}
                  >
                    <ShippingForm
                      defaultValues={{
                        fullName: user?.name,
                        ...shippingData,
                      }}
                      onSubmit={handleShippingSubmit}
                      isSubmitting={isSubmitting}
                    />
                  </CheckoutStep>

                  {/* Step 2: Delivery */}
                  <CheckoutStep
                    stepNumber={2}
                    title={steps[1].title}
                    description={steps[1].description}
                    isActive={currentStep === 1}
                    isCompleted={currentStep > 1}
                  >
                    <DeliveryForm
                      onSubmit={handleDeliverySubmit}
                      onBack={() => setCurrentStep(currentStep - 1)}
                      isSubmitting={isSubmitting}
                      deliveryFee={defaultDeliveryFee}
                    />
                  </CheckoutStep>

                  {/* Step 3: Payment */}
                  <CheckoutStep
                    stepNumber={3}
                    title={steps[2].title}
                    description={steps[2].description}
                    isActive={currentStep === 2}
                    isCompleted={false}
                  >
                    <SquarePaymentForm
                      data={{
                        dealUuid: checkoutState.dealId || deal?.id || "",
                        utilisateurUuid: user?.id || "",
                        montant: total,
                        quantity: qty,
                        rue: shippingData?.address || "",
                        ville: shippingData?.city || "",
                        codePostal: shippingData?.postalCode || "",
                        numeroPhone: shippingData?.phone || "",
                        appartement: shippingData?.complementAddress || "",
                        homeDelivery: deliveryData?.deliveryMethod === "home",
                      }}
                      onSuccess={handleSquarePaymentSuccess}
                      onError={handleSquarePaymentError}
                      onBack={() => setCurrentStep(currentStep - 1)}
                    />
                  </CheckoutStep>
                </VStack>

                {apiError && (
                  <div className="mt-6 p-4 bg-destructive/10 border border-destructive/30 rounded-lg text-destructive text-sm">
                    {apiError}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Summary Sidebar */}
          <div className="lg:col-span-1">
            <div className="sticky top-8">
              <VStack spacing={10}>
                <OrderSummary
                  deal={deal}
                  qty={qty}
                  subtotal={subtotal}
                  serviceFees={serviceFees}
                  tax={tax}
                  deliveryFee={deliveryFee}
                  total={total}
                />
                <TrustIndicators />
                <HelpSection
                  onBack={() => navigate(-1)}
                  onHome={() => navigate("/")}
                />
              </VStack>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
