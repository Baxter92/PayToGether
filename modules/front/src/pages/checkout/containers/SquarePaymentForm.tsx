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

interface SquarePaymentFormProps {
  dealUuid: string;
  utilisateurUuid: string;
  montant: number;
  onSuccess: (paymentId: string) => void;
  onError?: (error: string) => void;
  onBack?: () => void;
}

/**
 * Composant de formulaire de paiement Square
 * Supporte: Card, Google Pay, Apple Pay, Cash App Pay
 */
export default function SquarePaymentForm({
  dealUuid,
  utilisateurUuid,
  montant,
  onSuccess,
  onError,
  onBack,
}: SquarePaymentFormProps) {
  const navigate = useNavigate();
  const { isSquareLoaded, squareError, createPayment, isCreatingPayment } =
    useSquarePayment();

  const [payments, setPayments] = useState<any>(null);
  const [card, setCard] = useState<any>(null);
  const [selectedMethod, setSelectedMethod] =
    useState<SquarePaymentMethod>("card");
  const [initError, setInitError] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  // Nouveaux états pour la confirmation
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  const cardContainerRef = useRef<HTMLDivElement>(null);

  // Application ID Square (à configurer depuis les variables d'environnement)
  const SQUARE_APPLICATION_ID =
    import.meta.env.VITE_SQUARE_APPLICATION_ID || "sandbox-sq0idb-YOUR_APP_ID";
  const SQUARE_LOCATION_ID = import.meta.env.VITE_SQUARE_LOCATION_ID || "main";

  // Initialiser Square Payments
  useEffect(() => {
    if (!isSquareLoaded || !window.Square) return;

    const initializeSquare = async () => {
      try {
        const paymentsInstance = window.Square.payments(
          SQUARE_APPLICATION_ID,
          SQUARE_LOCATION_ID,
        );
        setPayments(paymentsInstance);

        // Initialiser le formulaire de carte
        const cardInstance = await paymentsInstance.card();
        await cardInstance.attach(cardContainerRef.current);
        setCard(cardInstance);

        setIsInitialized(true);
      } catch (error: any) {
        console.error("Error initializing Square:", error);
        setInitError(error.message || "Erreur d'initialisation Square");
        onError?.(error.message);
      }
    };

    initializeSquare();

    return () => {
      // Cleanup
      if (card) {
        card.destroy();
      }
    };
  }, [isSquareLoaded, SQUARE_APPLICATION_ID, SQUARE_LOCATION_ID]);

  // Ouvrir le dialog de confirmation
  const handleInitiatePayment = () => {
    setShowConfirmDialog(true);
  };

  // Traiter le paiement après confirmation
  const handleConfirmPayment = async () => {
    setShowConfirmDialog(false);
    setIsProcessing(true);

    if (!card || !payments) {
      onError?.("Square Payment n'est pas initialisé");
      setIsProcessing(false);
      return;
    }

    try {
      // Tokeniser le moyen de paiement
      const result = await card.tokenize();

      if (result.status === "OK" && result.token) {
        // Envoyer le token au backend
        const paymentData = {
          dealUuid,
          utilisateurUuid,
          montant,
          squareToken: result.token,
          methodePaiement: getMethodePaiementEnum(selectedMethod),
          locationId: SQUARE_LOCATION_ID,
        } as SquarePaymentRequest;

        const response = await createPayment(paymentData);

        // Gestion des différents statuts de paiement
        if (
          response.statut === "CONFIRME" ||
          response.statut === "PROCESSING" ||
          response.statut === "EN_ATTENTE"
        ) {
          onSuccess(response.uuid);
          // Redirection vers la page de succès
          navigate("/checkout/payment-success", { replace: true });
        } else if (response.statut === "ECHOUE") {
          setIsProcessing(false);
          onError?.(response.messageErreur || "Paiement échoué");
        } else {
          // Statut inattendu
          console.warn("Statut de paiement inattendu:", response.statut);
          setIsProcessing(false);
          onError?.(`Statut de paiement inattendu: ${response.statut}`);
        }
      } else {
        // Gérer les erreurs de tokenisation
        const errors = result.errors?.map((e: any) => e.message).join(", ");
        setIsProcessing(false);
        onError?.(errors || "Erreur lors de la tokenisation");
      }
    } catch (error: any) {
      console.error("Payment error:", error);
      setIsProcessing(false);
      onError?.(error.message || "Erreur lors du paiement");
    }
  };

  // Convertir la méthode de paiement en enum backend
  const getMethodePaiementEnum = (method: SquarePaymentMethod): string => {
    const map = {
      card: "SQUARE_CARD",
      googlePay: "SQUARE_GOOGLE_PAY",
      applePay: "SQUARE_APPLE_PAY",
      cashAppPay: "SQUARE_CASH_APP_PAY",
    };
    return map[method];
  };

  // Affichage pendant le chargement
  if (!isSquareLoaded) {
    return (
      <Card>
        <CardContent className="p-6">
          <HStack spacing={3} align="center" justify="center">
            <Loader2 className="w-5 h-5 animate-spin" />
            <span>Chargement du module de paiement...</span>
          </HStack>
        </CardContent>
      </Card>
    );
  }

  // Affichage en cas d'erreur
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
      {/* Loader de traitement amélioré */}
      {isProcessing && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <Card className="w-full max-w-md shadow-2xl border-2">
            <CardContent className="p-8">
              <VStack spacing={6} align="center">
                {/* Animation de loader */}
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

                {/* Texte de chargement */}
                <VStack spacing={2} align="center">
                  <h3 className="text-xl font-semibold">
                    Traitement du paiement
                  </h3>
                  <p className="text-sm text-muted-foreground text-center">
                    Veuillez patienter pendant que nous sécurisons votre
                    transaction...
                  </p>
                </VStack>

                {/* Barre de progression animée */}
                <div className="w-full bg-muted rounded-full h-2 overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-primary via-primary/60 to-primary animate-[pulse_1.5s_ease-in-out_infinite] w-2/3" />
                </div>

                {/* Indicateurs de sécurité */}
                <HStack spacing={4} className="text-xs text-muted-foreground">
                  <HStack spacing={1}>
                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                    <span>Chiffré</span>
                  </HStack>
                  <HStack spacing={1}>
                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                    <span>Sécurisé</span>
                  </HStack>
                  <HStack spacing={1}>
                    <ShieldCheck className="w-4 h-4 text-green-500" />
                    <span>Protégé</span>
                  </HStack>
                </HStack>
              </VStack>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Dialog de confirmation */}
      <AlertDialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-primary" />
              Confirmer le paiement
            </AlertDialogTitle>
            <AlertDialogDescription>
              <VStack spacing={4} className="pt-4">
                <div className="bg-primary/10 p-4 rounded-lg">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium">Montant total :</span>
                    <span className="text-2xl font-bold text-primary">
                      {montant.toFixed(2)} CAD
                    </span>
                  </div>
                </div>

                <div className="text-left space-y-2">
                  <p className="text-sm">
                    Vous êtes sur le point d'effectuer un paiement sécurisé via
                    Square.
                  </p>
                  <div className="flex items-start gap-2 text-xs text-muted-foreground">
                    <CheckCircle2 className="w-4 h-4 mt-0.5 text-green-500 flex-shrink-0" />
                    <span>
                      Vos informations sont protégées par un chiffrement de
                      niveau bancaire
                    </span>
                  </div>
                </div>
              </VStack>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isProcessing}>
              Annuler
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmPayment}
              disabled={isProcessing}
              className="bg-gradient-to-r from-primary to-primary/80"
            >
              <HStack spacing={2}>
                <ShieldCheck className="w-4 h-4" />
                <span>Confirmer le paiement</span>
              </HStack>
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <Card>
        <CardHeader>
          <CardTitle>Paiement sécurisé via Square</CardTitle>
        </CardHeader>
        <CardContent>
          <VStack spacing={4}>
            {/* Montant à payer */}
            <div className="bg-primary/10 p-4 rounded-md">
              <div className="text-sm text-muted-foreground">
                Montant à payer
              </div>
              <div className="text-2xl font-bold">{montant.toFixed(2)} CAD</div>
            </div>

            {/* Sélection de la méthode de paiement */}
            <div>
              <label className="text-sm font-medium mb-2 block">
                Méthode de paiement
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
                  Carte
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
                  Google Pay
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
                  Apple Pay
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
                  Cash App
                </Button>
              </div>
            </div>

            {/* Container pour le formulaire Square Card */}
            {selectedMethod === "card" && (
              <div>
                <label className="text-sm font-medium mb-2 block">
                  Informations de carte
                </label>
                <div
                  ref={cardContainerRef}
                  id="card-container"
                  className="border rounded-md p-3 bg-white"
                />
              </div>
            )}

            {/* Bouton de paiement */}
            <HStack spacing={2} justify="end">
              {onBack && (
                <Button
                  variant="outline"
                  onClick={onBack}
                  disabled={isProcessing}
                >
                  Retour
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
                    Traitement en cours...
                  </>
                ) : (
                  <HStack spacing={2}>
                    <ShieldCheck className="w-5 h-5" />
                    <span>Payer {montant.toFixed(2)} CAD</span>
                  </HStack>
                )}
              </Button>
            </HStack>

            {/* Informations de sécurité */}
            <div className="text-xs text-muted-foreground text-center space-y-1">
              <p className="flex items-center justify-center gap-1">
                <ShieldCheck className="w-4 h-4 text-green-500" />
                Paiement sécurisé par Square
              </p>
              <p>
                Vos informations de paiement ne sont jamais stockées sur nos
                serveurs
              </p>
            </div>
          </VStack>
        </CardContent>
      </Card>
    </>
  );
}
