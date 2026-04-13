import { useState, useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { apiClient } from "@/common/api/services/apiClient";

// Types pour Square Payment
export type SquarePaymentMethod =
  | "card"
  | "googlePay"
  | "applePay"
  | "cashAppPay";

export interface SquarePaymentRequest {
  dealUuid: string;
  utilisateurUuid: string;
  montant: number; // Montant total de la commande
  adresse: {
    rue: string;
    ville: string;
    codePostal: string;
    numeroPhone: string;
    appartement: string;
    homeDelivery: boolean;
  };
  nombreDePart: number;
  squareToken: string;
  methodePaiement:
    | "SQUARE_CARD"
    | "SQUARE_GOOGLE_PAY"
    | "SQUARE_APPLE_PAY"
    | "SQUARE_CASH_APP_PAY";
  locationId?: string;
}

export interface SquarePaymentResponse {
  uuid: string;
  montant: number;
  statut: string;
  methodePaiement: string;
  transactionId: string | null;
  squarePaymentId: string | null;
  squareOrderId: string | null;
  squareLocationId: string;
  squareReceiptUrl: string | null;
  messageErreur?: string | null;
  utilisateurUuid: string;
  commandeUuid: string;
  datePaiement: string;
  dateCreation: string | null;
  dateModification: string | null;
}

/**
 * Hook pour gérer les paiements Square
 */
export const useSquarePayment = () => {
  const [isSquareLoaded, setIsSquareLoaded] = useState(false);
  const [squareError, setSquareError] = useState<string | null>(null);

  // Charger le SDK Square
  useEffect(() => {
    const loadSquareSDK = async () => {
      try {
        // Vérifier si Square est déjà chargé
        if (window.Square) {
          setIsSquareLoaded(true);
          return;
        }

        // Déterminer l'environnement (SANDBOX ou PRODUCTION)
        const environment = import.meta.env.VITE_SQUARE_ENVIRONMENT || "SANDBOX";
        const isProduction = environment.toUpperCase() === "PRODUCTION";

        // Charger le script Square correspondant à l'environnement
        const script = document.createElement("script");
        script.src = isProduction
          ? "https://web.squarecdn.com/v1/square.js"
          : "https://sandbox.web.squarecdn.com/v1/square.js";
        script.async = true;
        script.onload = () => {
          console.log(`Square SDK chargé (${environment})`);
          setIsSquareLoaded(true);
        };
        script.onerror = () => {
          setSquareError("Erreur lors du chargement du SDK Square");
        };
        document.body.appendChild(script);
      } catch (error) {
        setSquareError("Erreur lors de l'initialisation de Square");
        console.error("Square SDK loading error:", error);
      }
    };

    loadSquareSDK();
  }, []);

  // Mutation pour créer un paiement Square
  const createPaymentMutation = useMutation<
    SquarePaymentResponse,
    Error,
    SquarePaymentRequest
  >({
    mutationFn: async (paymentData) => {
      const response = await apiClient.post<SquarePaymentResponse>(
        "/square-payments",
        { body: paymentData },
      );
      return response;
    },
  });

  // Mutation pour vérifier le statut d'un paiement
  const checkPaymentStatusMutation = useMutation<
    SquarePaymentResponse,
    Error,
    string
  >({
    mutationFn: async (paiementUuid) => {
      const response = await apiClient.get<SquarePaymentResponse>(
        `/square-payments/${paiementUuid}/status`,
      );
      return response;
    },
  });

  // Mutation pour rembourser un paiement
  const refundPaymentMutation = useMutation<
    SquarePaymentResponse,
    Error,
    string
  >({
    mutationFn: async (paiementUuid) => {
      const response = await apiClient.post<SquarePaymentResponse>(
        `/square-payments/${paiementUuid}/refund`,
        { body: {} },
      );
      return response;
    },
  });

  return {
    isSquareLoaded,
    squareError,
    createPayment: createPaymentMutation.mutateAsync,
    isCreatingPayment: createPaymentMutation.isPending,
    paymentError: createPaymentMutation.error,
    checkPaymentStatus: checkPaymentStatusMutation.mutateAsync,
    refundPayment: refundPaymentMutation.mutateAsync,
  };
};

// Déclarer le type Window pour TypeScript
declare global {
  interface Window {
    Square?: any;
  }
}
