/**
 * Configuration de test pour Square Payment
 * Fournit des mocks et utilitaires pour tester les composants Square
 */

import { vi } from "vitest";

/**
 * Mock du SDK Square
 */
export const mockSquareSDK = () => {
  const mockCard = {
    attach: vi.fn().mockResolvedValue(undefined),
    tokenize: vi.fn().mockResolvedValue({
      status: "OK",
      token: "test-token-123",
    }),
    destroy: vi.fn(),
  };

  const mockPayments = {
    card: vi.fn().mockResolvedValue(mockCard),
  };

  (window as any).Square = {
    payments: vi.fn(() => mockPayments),
  };

  return { mockCard, mockPayments };
};

/**
 * Mock des réponses API Square
 */
export const mockSquareAPIResponses = {
  createPaymentSuccess: {
    uuid: "payment-123",
    montant: 99.99,
    statut: "CONFIRME",
    methodePaiement: "SQUARE_CARD",
    squarePaymentId: "square-pay-123",
    squareOrderId: "square-order-123",
    squareLocationId: "location-123",
    squareReceiptUrl: "https://square.com/receipt/123",
    utilisateurUuid: "user-123",
    commandeUuid: "order-123",
    datePaiement: new Date().toISOString(),
    dateCreation: new Date().toISOString(),
    dateModification: new Date().toISOString(),
  },

  createPaymentError: {
    errorCode: "paiement.traitement.echec",
    message: "Le paiement a échoué",
    status: 400,
  },

  refundSuccess: {
    uuid: "payment-123",
    statut: "REFUNDED",
    messageErreur: "Remboursé - Refund ID: refund-123",
  },
};

/**
 * Mock de l'environnement Square
 */
export const mockSquareEnvironment = () => {
  const originalEnv = import.meta.env;

  Object.defineProperty(import.meta, "env", {
    value: {
      ...originalEnv,
      VITE_SQUARE_APPLICATION_ID: "sandbox-sq0idb-test-app-id",
      VITE_SQUARE_LOCATION_ID: "test-location-id",
      VITE_SQUARE_ENVIRONMENT: "SANDBOX",
    },
    writable: true,
  });

  return () => {
    Object.defineProperty(import.meta, "env", {
      value: originalEnv,
      writable: true,
    });
  };
};

/**
 * Utilitaire pour attendre le chargement de Square
 */
export const waitForSquareLoad = async () => {
  return new Promise((resolve) => {
    const checkSquare = () => {
      if ((window as any).Square) {
        resolve(true);
      } else {
        setTimeout(checkSquare, 50);
      }
    };
    checkSquare();
  });
};

/**
 * Utilitaire pour simuler un paiement Square
 */
export const simulateSquarePayment = async (
  success: boolean = true,
  token: string = "test-token-123"
) => {
  const mockCard = {
    attach: vi.fn(),
    tokenize: vi.fn().mockResolvedValue(
      success
        ? { status: "OK", token }
        : {
            status: "ERROR",
            errors: [{ message: "Card declined" }],
          }
    ),
    destroy: vi.fn(),
  };

  return mockCard;
};

/**
 * Données de test pour le checkout
 */
export const testCheckoutData = {
  deal: {
    uuid: "deal-123",
    id: "deal-123",
    title: "Test Deal",
    pricePerPart: 50.0,
  },
  qty: 2,
  dealId: "deal-123",
  user: {
    uuid: "user-123",
    name: "Test User",
    email: "test@example.com",
  },
  shipping: {
    fullName: "John Doe",
    phone: "+1234567890",
    address: "123 Main St",
    city: "Montreal",
    postalCode: "H1A 1A1",
  },
  delivery: {
    deliveryMethod: "home" as const,
  },
};

/**
 * Utilitaire pour nettoyer les mocks Square
 */
export const cleanupSquareMocks = () => {
  delete (window as any).Square;
  vi.clearAllMocks();
};

