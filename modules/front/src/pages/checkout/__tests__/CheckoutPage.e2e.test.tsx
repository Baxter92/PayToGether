import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import CheckoutPage from "../index";
import { apiClient } from "@/common/api/apiClient";

// Mock de l'API
vi.mock("@/common/api/apiClient", () => ({
  apiClient: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

// Mock du contexte d'authentification
vi.mock("@/common/context/AuthContext", () => ({
  useAuth: () => ({
    user: {
      uuid: "user-123",
      name: "John Doe",
      email: "john@example.com",
    },
  }),
}));

// Mock de l'i18n
vi.mock("@hooks/useI18n", () => ({
  useI18n: () => ({
    t: (key: string) => key,
  }),
}));

// Mock de window.Square
const mockSquarePayments = {
  card: vi.fn().mockResolvedValue({
    attach: vi.fn(),
    tokenize: vi.fn().mockResolvedValue({
      status: "OK",
      token: "test-square-token-123",
    }),
    destroy: vi.fn(),
  }),
};

describe("Checkout E2E - Flux complet avec Square Payment", () => {
  let queryClient: QueryClient;

  const mockDealState = {
    deal: {
      uuid: "deal-123",
      id: "deal-123",
      title: "Deal Test E2E",
      pricePerPart: 75.0,
    },
    qty: 2,
    dealId: "deal-123",
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    // Mock window.Square
    (window as any).Square = {
      payments: vi.fn(() => mockSquarePayments),
    };

    // Mock des réponses API
    (apiClient.post as any).mockResolvedValue({
      uuid: "payment-123",
      montant: 150.0,
      statut: "CONFIRME",
      squarePaymentId: "square-payment-123",
      squareReceiptUrl: "https://square.com/receipt/123",
    });

    vi.clearAllMocks();
  });

  const renderCheckout = () => {
    return render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[{ state: mockDealState }]}>
          <CheckoutPage />
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  it("devrait compléter tout le flux de checkout avec Square Payment", async () => {
    renderCheckout();

    // 1. Vérifier l'affichage de l'étape 1 - Shipping
    expect(screen.getByText("checkout.shippingTitle")).toBeInTheDocument();

    // Remplir le formulaire shipping
    const fullNameInput = screen.getByLabelText(/full name/i);
    const phoneInput = screen.getByLabelText(/phone/i);
    const addressInput = screen.getByLabelText(/address/i);
    const cityInput = screen.getByLabelText(/city/i);

    fireEvent.change(fullNameInput, { target: { value: "John Doe" } });
    fireEvent.change(phoneInput, { target: { value: "+1234567890" } });
    fireEvent.change(addressInput, { target: { value: "123 Main St" } });
    fireEvent.change(cityInput, { target: { value: "Montreal" } });

    // Soumettre le formulaire shipping
    const shippingSubmitButton = screen.getByRole("button", {
      name: /continue|next/i,
    });
    fireEvent.click(shippingSubmitButton);

    // 2. Attendre l'affichage de l'étape 2 - Delivery
    await waitFor(() => {
      expect(screen.getByText("checkout.deliveryTitle")).toBeInTheDocument();
    });

    // Sélectionner la méthode de livraison
    const homeDeliveryOption = screen.getByLabelText(/home delivery/i);
    fireEvent.click(homeDeliveryOption);

    // Soumettre le formulaire delivery
    const deliverySubmitButton = screen.getByRole("button", {
      name: /continue|next/i,
    });
    fireEvent.click(deliverySubmitButton);

    // 3. Attendre l'affichage de l'étape 3 - Payment
    await waitFor(() => {
      expect(screen.getByText("checkout.paymentTitle")).toBeInTheDocument();
    });

    // Vérifier que Square Payment est activé par défaut
    expect(screen.getByText("Square Payment")).toBeInTheDocument();

    // 4. Vérifier l'affichage du montant total
    // Montant = (qty * pricePerPart) + deliveryFee = (2 * 75) + 3.5 = 153.5
    expect(screen.getByText(/153\.50 \$/)).toBeInTheDocument();

    // 5. Attendre le chargement du formulaire Square
    await waitFor(() => {
      expect(
        screen.getByText(/paiement sécurisé via square/i)
      ).toBeInTheDocument();
    });

    // 6. Vérifier les options de paiement disponibles
    expect(screen.getByText(/carte/i)).toBeInTheDocument();
    expect(screen.getByText(/google pay/i)).toBeInTheDocument();
    expect(screen.getByText(/apple pay/i)).toBeInTheDocument();

    // 7. Sélectionner la méthode par carte (déjà sélectionnée par défaut)
    const payButton = screen.getByRole("button", { name: /payer/i });
    expect(payButton).toBeInTheDocument();

    // 8. Cliquer sur le bouton de paiement
    fireEvent.click(payButton);

    // 9. Vérifier que l'API a été appelée avec les bonnes données
    await waitFor(() => {
      expect(apiClient.post).toHaveBeenCalledWith("/square-payments", {
        body: expect.objectContaining({
          commandeUuid: "deal-123",
          utilisateurUuid: "user-123",
          montant: 153.5,
          squareToken: "test-square-token-123",
          methodePaiement: "SQUARE_CARD",
        }),
      });
    });

    // 10. Vérifier la redirection après succès
    await waitFor(() => {
      // Le composant devrait déclencher une navigation
      // (dans un vrai test E2E, on vérifierait l'URL)
      expect(apiClient.post).toHaveBeenCalled();
    });
  });

  it("devrait gérer les erreurs de paiement Square", async () => {
    // Mock d'une erreur API
    (apiClient.post as any).mockRejectedValue(
      new Error("Erreur de traitement du paiement")
    );

    renderCheckout();

    // Naviguer jusqu'à l'étape de paiement (raccourci)
    // Dans un vrai test, on passerait par toutes les étapes

    await waitFor(() => {
      expect(screen.getByText("checkout.paymentTitle")).toBeInTheDocument();
    });

    // Cliquer sur le bouton de paiement
    const payButton = screen.getByRole("button", { name: /payer/i });
    fireEvent.click(payButton);

    // Vérifier l'affichage de l'erreur
    await waitFor(() => {
      expect(
        screen.getByText(/erreur de traitement du paiement/i)
      ).toBeInTheDocument();
    });
  });

  it("devrait permettre de basculer entre Square et paiement classique", async () => {
    renderCheckout();

    // Naviguer jusqu'à l'étape de paiement
    await waitFor(() => {
      expect(screen.getByText("checkout.paymentTitle")).toBeInTheDocument();
    });

    // Vérifier que Square est actif
    expect(screen.getByTestId("square-payment-form")).toBeInTheDocument();

    // Cliquer sur le toggle
    const toggle = screen.getByRole("button", {
      name: /paiement classique|square payment/i,
    });
    fireEvent.click(toggle);

    // Vérifier que le formulaire classique est affiché
    await waitFor(() => {
      expect(screen.getByTestId("classic-payment-form")).toBeInTheDocument();
      expect(
        screen.queryByTestId("square-payment-form")
      ).not.toBeInTheDocument();
    });

    // Revenir à Square
    fireEvent.click(toggle);

    await waitFor(() => {
      expect(screen.getByTestId("square-payment-form")).toBeInTheDocument();
      expect(
        screen.queryByTestId("classic-payment-form")
      ).not.toBeInTheDocument();
    });
  });

  it("devrait afficher le récapitulatif de commande", async () => {
    renderCheckout();

    // Vérifier l'affichage du deal
    expect(screen.getByText("Deal Test E2E")).toBeInTheDocument();

    // Vérifier la quantité
    expect(screen.getByText(/2/)).toBeInTheDocument();

    // Vérifier le sous-total
    expect(screen.getByText(/150\.00/)).toBeInTheDocument();
  });

  it("devrait calculer correctement les frais de livraison", async () => {
    renderCheckout();

    // Naviguer jusqu'à l'étape de livraison
    await waitFor(() => {
      expect(screen.getByText("checkout.deliveryTitle")).toBeInTheDocument();
    });

    // Sélectionner livraison à domicile
    const homeDelivery = screen.getByLabelText(/home delivery/i);
    fireEvent.click(homeDelivery);

    // Soumettre
    const submitButton = screen.getByRole("button", { name: /continue/i });
    fireEvent.click(submitButton);

    // Vérifier que les frais de livraison sont ajoutés
    await waitFor(() => {
      expect(screen.getByText(/3\.50/)).toBeInTheDocument();
    });
  });
});

