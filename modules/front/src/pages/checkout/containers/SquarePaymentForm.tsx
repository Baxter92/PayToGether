import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useSquarePayment } from "@/common/api/hooks/useSquarePayment";
import type {
  SquarePaymentMethod,
  SquarePaymentRequest,
} from "@/common/api/hooks/useSquarePayment";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/card";
import { Button } from "@components/ui/button";
import {
  AlertDialog,
  AlertDialogDescription,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from "@components/ui/alert-dialog";
import {
  Loader2,
  CreditCard,
  Smartphone,
  Apple,
  DollarSign,
  CheckCircle2,
  ShieldCheck,
} from "lucide-react";
import VStack from "@components/VStack";
import HStack from "@components/HStack";
import { useI18n } from "@/common/hooks/useI18n";

interface SquarePaymentFormProps {
  data: {
    dealUuid: string;
    utilisateurUuid: string;
    montant: number;
    rue: string;
    ville: string;
    codePostal: string;
    numeroPhone: string;
    appartement: string;
    homeDelivery: boolean;
    quantity: number;
  };
  onSuccess: (paymentId: string) => void;
  onError?: (error: string) => void;
  onBack?: () => void;
}

/**
 * Square payment form
 * Supports: Card, Google Pay, Apple Pay, Cash App Pay
 */
export default function SquarePaymentForm({
  data,
  onSuccess,
  onError,
  onBack,
}: SquarePaymentFormProps) {
  const navigate = useNavigate();
  const { t } = useI18n("checkout");
  const { isSquareLoaded, squareError, createPayment, isCreatingPayment } =
    useSquarePayment();

  const [payments, setPayments] = useState<any>(null);
  const cardRef = useRef<any>(null);
  const [selectedMethod, setSelectedMethod] =
    useState<SquarePaymentMethod>("card");
  const [initError, setInitError] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  const cardContainerRef = useRef<HTMLDivElement>(null);

  const SQUARE_APPLICATION_ID =
    import.meta.env.VITE_SQUARE_APPLICATION_ID || "sandbox-sq0idb-YOUR_APP_ID";
  const SQUARE_LOCATION_ID = import.meta.env.VITE_SQUARE_LOCATION_ID || "main";

  useEffect(() => {
    if (!isSquareLoaded || !window.Square) return;

    const initializeSquare = async () => {
      try {
        const paymentsInstance = window.Square.payments(
          SQUARE_APPLICATION_ID,
          SQUARE_LOCATION_ID,
        );
        setPayments(paymentsInstance);

        const cardInstance = await paymentsInstance.card();
        await cardInstance.attach(cardContainerRef.current);
        cardRef.current = cardInstance;

        setIsInitialized(true);
      } catch (error: any) {
        console.error("Error initializing Square:", error);
        setInitError(error.message || t("squarePayment.errors.init"));
        onError?.(error.message || t("squarePayment.errors.init"));
      }
    };

    initializeSquare();

    return () => {
      if (cardRef.current) {
        cardRef.current.destroy();
        cardRef.current = null;
      }
    };
  }, [isSquareLoaded, SQUARE_APPLICATION_ID, SQUARE_LOCATION_ID]);

  const handleInitiatePayment = () => {
    setShowConfirmDialog(true);
  };

  const handleConfirmPayment = async () => {
    setShowConfirmDialog(false);
    setIsProcessing(true);

    const card = cardRef.current;
    if (!card || !payments) {
      onError?.(t("squarePayment.errors.notInitialized"));
      setIsProcessing(false);
      return;
    }

    try {
      const result = await card.tokenize();

      if (result.status === "OK" && result.token) {
        const { dealUuid, utilisateurUuid, montant, ...rest } = data;
        const paymentData = {
          dealUuid,
          utilisateurUuid,
          montant,
          squareToken: result.token,
          adresse: rest,
          methodePaiement: getMethodePaiementEnum(selectedMethod),
          locationId: SQUARE_LOCATION_ID,
          nombreDePart: data.quantity,
        } as SquarePaymentRequest;

        const response = await createPayment(paymentData);

        if (
          response.statut === "CONFIRME" ||
          response.statut === "PROCESSING" ||
          response.statut === "EN_ATTENTE"
        ) {
          onSuccess(response.uuid);
          navigate("/checkout/payment-success", { replace: true });
        } else if (response.statut === "ECHOUE") {
          setIsProcessing(false);
          onError?.(
            response.messageErreur || t("squarePayment.errors.paymentFailed"),
          );
        } else {
          console.warn("Unexpected payment status:", response.statut);
          setIsProcessing(false);
          onError?.(
            t("squarePayment.errors.unexpectedStatus", {
              status: response.statut,
            }),
          );
        }
      } else {
        const errors = result.errors?.map((e: any) => e.message).join(", ");
        setIsProcessing(false);
        onError?.(errors || t("squarePayment.errors.tokenization"));
      }
    } catch (error: any) {
      console.error("Payment error:", error);
      setIsProcessing(false);
      onError?.(error.message || t("squarePayment.errors.generic"));
    }
  };

  const getMethodePaiementEnum = (method: SquarePaymentMethod): string => {
    const map = {
      card: "SQUARE_CARD",
      googlePay: "SQUARE_GOOGLE_PAY",
      applePay: "SQUARE_APPLE_PAY",
      cashAppPay: "SQUARE_CASH_APP_PAY",
    };
    return map[method];
  };

  if (!isSquareLoaded) {
    return (
      <Card>
        <CardContent className="p-6">
          <HStack spacing={3} align="center" justify="center">
            <Loader2 className="w-5 h-5 animate-spin" />
            <span>{t("squarePayment.loading")}</span>
          </HStack>
        </CardContent>
      </Card>
    );
  }

  if (squareError || initError) {
    return (
      <Card>
        <CardContent className="p-6">
          <AlertDialog>
            <AlertDialogDescription>
              {squareError || initError}
            </AlertDialogDescription>
          </AlertDialog>
        </CardContent>
      </Card>
    );
  }

  return (
    <>
      {isProcessing && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <Card className="w-full max-w-md shadow-2xl border-2">
            <CardContent className="p-8">
              <VStack spacing={6} align="center">
                <div className="relative">
                  <div className="absolute inset-0 bg-primary/20 rounded-full blur-2xl animate-pulse" />
                  <div className="relative">
                    <Loader2
                      className="w-20 h-20 text-primary animate-spin"
                      strokeWidth={2}
                    />
                    <div className="absolute inset-0 flex items-center justify-center">
                      <ShieldCheck className="w-10 h-10 text-primary/50" />
                    </div>
                  </div>
                </div>

                <VStack spacing={2} align="center">
                  <h3 className="text-xl font-semibold">
                    {t("squarePayment.processingTitle")}
                  </h3>
                  <p className="text-sm text-muted-foreground text-center">
                    {t("squarePayment.processingDescription")}
                  </p>
                </VStack>

                <div className="w-full bg-muted rounded-full h-2 overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-primary via-primary/60 to-primary animate-[pulse_1.5s_ease-in-out_infinite] w-2/3" />
                </div>

                <HStack spacing={4} className="text-xs text-muted-foreground">
                  <HStack spacing={1}>
                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                    <span>{t("squarePayment.security.encrypted")}</span>
                  </HStack>
                  <HStack spacing={1}>
                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                    <span>{t("squarePayment.security.secured")}</span>
                  </HStack>
                  <HStack spacing={1}>
                    <ShieldCheck className="w-4 h-4 text-green-500" />
                    <span>{t("squarePayment.security.protected")}</span>
                  </HStack>
                </HStack>
              </VStack>
            </CardContent>
          </Card>
        </div>
      )}

      <AlertDialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-primary" />
              {t("squarePayment.confirmTitle")}
            </AlertDialogTitle>
            <AlertDialogDescription className="w-full">
              <VStack spacing={4} className="pt-4 w-full">
                <div className="bg-primary/10 p-4 rounded-lg">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium">
                      {t("squarePayment.confirmAmountLabel")}
                    </span>
                    <span className="text-2xl font-bold text-primary">
                      {data.montant.toFixed(2)} CAD
                    </span>
                  </div>
                </div>

                <div className="text-left space-y-2">
                  <p className="text-sm">
                    {t("squarePayment.confirmDescription")}
                  </p>
                  <div className="flex items-start gap-2 text-xs text-muted-foreground">
                    <CheckCircle2 className="w-4 h-4 mt-0.5 text-green-500 flex-shrink-0" />
                    <span>{t("squarePayment.confirmSecurityNote")}</span>
                  </div>
                </div>
              </VStack>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isProcessing}>
              {t("squarePayment.cancel")}
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmPayment}
              disabled={isProcessing}
              className="bg-gradient-to-r from-primary to-primary/80 p-2 hover:from-primary/90 hover:to-primary/70 transition-all duration-300 rounded-md"
            >
              <HStack spacing={2}>
                <ShieldCheck className="w-4 h-4" />
                <span>{t("squarePayment.confirmButton")}</span>
              </HStack>
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <Card>
        <CardHeader>
          <CardTitle>{t("squarePayment.title")}</CardTitle>
        </CardHeader>
        <CardContent>
          <VStack spacing={4}>
            <div className="bg-primary/10 p-4 rounded-md">
              <div className="text-sm text-muted-foreground">
                {t("squarePayment.amountLabel")}
              </div>
              <div className="text-2xl font-bold">
                {data.montant.toFixed(2)} CAD
              </div>
            </div>

            <div>
              <label className="text-sm font-medium mb-2 block">
                {t("squarePayment.methodLabel")}
              </label>
              <div className="grid grid-cols-2 gap-2">
                <Button
                  type="button"
                  variant={selectedMethod === "card" ? "default" : "outline"}
                  onClick={() => setSelectedMethod("card")}
                  className="flex items-center gap-2"
                  disabled={isProcessing}
                >
                  <CreditCard className="w-4 h-4" />
                  {t("squarePayment.methods.card")}
                </Button>
                <Button
                  type="button"
                  variant={
                    selectedMethod === "googlePay" ? "default" : "outline"
                  }
                  onClick={() => setSelectedMethod("googlePay")}
                  className="flex items-center gap-2"
                  disabled={isProcessing}
                >
                  <Smartphone className="w-4 h-4" />
                  {t("squarePayment.methods.googlePay")}
                </Button>
                <Button
                  type="button"
                  variant={
                    selectedMethod === "applePay" ? "default" : "outline"
                  }
                  onClick={() => setSelectedMethod("applePay")}
                  className="flex items-center gap-2"
                  disabled={isProcessing}
                >
                  <Apple className="w-4 h-4" />
                  {t("squarePayment.methods.applePay")}
                </Button>
                <Button
                  type="button"
                  variant={
                    selectedMethod === "cashAppPay" ? "default" : "outline"
                  }
                  onClick={() => setSelectedMethod("cashAppPay")}
                  className="flex items-center gap-2"
                  disabled={isProcessing}
                >
                  <DollarSign className="w-4 h-4" />
                  {t("squarePayment.methods.cashAppPay")}
                </Button>
              </div>
            </div>

            {selectedMethod === "card" && (
              <div>
                <label className="text-sm font-medium mb-2 block">
                  {t("squarePayment.cardInfo")}
                </label>
                <div
                  ref={cardContainerRef}
                  id="card-container"
                  className="border rounded-md p-3 bg-white"
                />
              </div>
            )}

            <HStack spacing={2} justify="end">
              {onBack && (
                <Button
                  variant="outline"
                  onClick={onBack}
                  disabled={isProcessing}
                >
                  {t("squarePayment.back")}
                </Button>
              )}
              <Button
                onClick={handleInitiatePayment}
                disabled={isCreatingPayment || !isInitialized || isProcessing}
                className="flex-1 bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all duration-300"
                size="lg"
              >
                {isProcessing ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    {t("squarePayment.processingButton")}
                  </>
                ) : (
                  <HStack spacing={2}>
                    <ShieldCheck className="w-5 h-5" />
                    <span>
                      {t("squarePayment.payButton", {
                        amount: data.montant.toFixed(2),
                      })}
                    </span>
                  </HStack>
                )}
              </Button>
            </HStack>

            <div className="text-xs text-muted-foreground text-center space-y-1">
              <p className="flex items-center justify-center gap-1">
                <ShieldCheck className="w-4 h-4 text-green-500" />
                {t("squarePayment.secureBySquare")}
              </p>
              <p>{t("squarePayment.noStorageNotice")}</p>
            </div>
          </VStack>
        </CardContent>
      </Card>
    </>
  );
}
