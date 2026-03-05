import { useState, useEffect, useRef } from "react";
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
} from "@components/ui/alert-dialog";
import {
  Loader2,
  CreditCard,
  Smartphone,
  Apple,
  DollarSign,
} from "lucide-react";
import VStack from "@components/VStack";
import HStack from "@components/HStack";

interface SquarePaymentFormProps {
  commandeUuid: string;
  utilisateurUuid: string;
  montant: number;
  onSuccess: (paymentId: string) => void;
  onError?: (error: string) => void;
}

/**
 * Composant de formulaire de paiement Square
 * Supporte: Card, Google Pay, Apple Pay, Cash App Pay
 */
export default function SquarePaymentForm({
  commandeUuid,
  utilisateurUuid,
  montant,
  onSuccess,
  onError,
}: SquarePaymentFormProps) {
  const { isSquareLoaded, squareError, createPayment, isCreatingPayment } =
    useSquarePayment();

  const [payments, setPayments] = useState<any>(null);
  const [card, setCard] = useState<any>(null);
  const [selectedMethod, setSelectedMethod] =
    useState<SquarePaymentMethod>("card");
  const [initError, setInitError] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

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

  // Traiter le paiement
  const handlePayment = async () => {
    if (!card || !payments) {
      onError?.("Square Payment n'est pas initialisé");
      return;
    }

    try {
      // Tokeniser le moyen de paiement
      const result = await card.tokenize();

      if (result.status === "OK" && result.token) {
        // Envoyer le token au backend
        const paymentData = {
          commandeUuid,
          utilisateurUuid,
          montant,
          squareToken: result.token,
          methodePaiement: getMethodePaiementEnum(selectedMethod),
          locationId: SQUARE_LOCATION_ID,
        } as SquarePaymentRequest;

        const response = await createPayment(paymentData);

        if (
          response.statut === "CONFIRME" ||
          response.statut === "PROCESSING"
        ) {
          onSuccess(response.uuid);
        } else if (response.statut === "ECHOUE") {
          onError?.(response.messageErreur || "Paiement échoué");
        }
      } else {
        // Gérer les erreurs de tokenisation
        const errors = result.errors?.map((e: any) => e.message).join(", ");
        onError?.(errors || "Erreur lors de la tokenisation");
      }
    } catch (error: any) {
      console.error("Payment error:", error);
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
    <Card>
      <CardHeader>
        <CardTitle>Paiement sécurisé via Square</CardTitle>
      </CardHeader>
      <CardContent>
        <VStack spacing={4}>
          {/* Montant à payer */}
          <div className="bg-primary/10 p-4 rounded-md">
            <div className="text-sm text-muted-foreground">Montant à payer</div>
            <div className="text-2xl font-bold">{montant.toFixed(2)} $</div>
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
              >
                <CreditCard className="w-4 h-4" />
                Carte
              </Button>
              <Button
                type="button"
                variant={selectedMethod === "googlePay" ? "default" : "outline"}
                onClick={() => setSelectedMethod("googlePay")}
                className="flex items-center gap-2"
              >
                <Smartphone className="w-4 h-4" />
                Google Pay
              </Button>
              <Button
                type="button"
                variant={selectedMethod === "applePay" ? "default" : "outline"}
                onClick={() => setSelectedMethod("applePay")}
                className="flex items-center gap-2"
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
          <Button
            onClick={handlePayment}
            disabled={isCreatingPayment || !isInitialized}
            className="w-full"
            size="lg"
          >
            {isCreatingPayment ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Traitement en cours...
              </>
            ) : (
              `Payer ${montant.toFixed(2)} $`
            )}
          </Button>

          {/* Informations de sécurité */}
          <div className="text-xs text-muted-foreground text-center">
            <p>🔒 Paiement sécurisé par Square</p>
            <p>
              Vos informations de paiement ne sont jamais stockées sur nos
              serveurs
            </p>
          </div>
        </VStack>
      </CardContent>
    </Card>
  );
}
