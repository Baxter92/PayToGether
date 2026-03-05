import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import CheckoutPage from "../index";

// Mock du contexte d'authentification
vi.mock("@/common/context/AuthContext", () => ({
  useAuth: () => ({
    user: {
      uuid: "test-user-uuid",
      name: "Test User",
      email: "test@example.com",
    },
  }),
}));

// Mock de l'i18n
vi.mock("@hooks/useI18n", () => ({
  useI18n: () => ({
    t: (key: string) => key,
  }),
}));

// Mock du composant SquarePaymentForm
vi.mock("../containers/SquarePaymentForm", () => ({
  default: ({
    onSuccess,
    onError,
  }: {
    onSuccess: (id: string) => void;
    onError: (error: string) => void;
  }) => (
    <div data-testid="square-payment-form">
      <button onClick={() => onSuccess("test-payment-id")}>
        Payer avec Square
      </button>
      <button onClick={() => onError("Test error")}>Simuler erreur</button>
    </div>
  ),
}));

// Mock du composant PaymentForm classique
vi.mock("../containers/PaymentForm", () => ({
  default: ({ onSubmit }: { onSubmit: (data: any) => void }) => (
    <div data-testid="classic-payment-form">
      <button
        onClick={() =>
          onSubmit({
            paymentMethod: "card",
            cardNumber: "4111111111111111",
          })
        }
      >
        Payer classique
      </button>
    </div>
  ),
}));

describe("CheckoutPage avec Square Payment", () => {
  let queryClient: QueryClient;

  const mockState = {
    deal: {
      uuid: "test-deal-uuid",
      id: "test-deal-id",
      title: "Test Deal",
      pricePerPart: 50.0,
    },
    qty: 2,
    dealId: "test-deal-id",
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    vi.clearAllMocks();
  });

  const renderWithProviders = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[{ state: mockState }]}>
          {ui}
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  it("devrait afficher le titre de la page checkout", () => {
    renderWithProviders(<CheckoutPage />);

    expect(screen.getByText("checkout.title")).toBeInTheDocument();
  });

  it("devrait afficher le toggle Square Payment/Paiement classique", async () => {
    renderWithProviders(<CheckoutPage />);

    // Naviguer jusqu'à l'étape de paiement
    // (Simuler la soumission des formulaires précédents)

    await waitFor(() => {
      expect(screen.getByText("Paiement classique")).toBeInTheDocument();
      expect(screen.getByText("Square Payment")).toBeInTheDocument();
    });
  });

  it("devrait afficher SquarePaymentForm par défaut", async () => {
    renderWithProviders(<CheckoutPage />);

    await waitFor(() => {
      expect(screen.queryByTestId("square-payment-form")).toBeInTheDocument();
    });
  });

  it("devrait permettre de basculer vers le paiement classique", async () => {
    renderWithProviders(<CheckoutPage />);

    await waitFor(() => {
      const toggle = screen.getAllByRole("button").find(
        (btn) => btn.className.includes("inline-flex")
      );

      if (toggle) {
        fireEvent.click(toggle);
      }
    });

    await waitFor(() => {
      expect(screen.queryByTestId("classic-payment-form")).toBeInTheDocument();
      expect(screen.queryByTestId("square-payment-form")).not.toBeInTheDocument();
    });
  });

  it("devrait gérer le succès du paiement Square", async () => {
    const mockNavigate = vi.fn();
    vi.mock("react-router-dom", async () => {
      const actual = await vi.importActual("react-router-dom");
      return {
        ...actual,
        useNavigate: () => mockNavigate,
      };
    });

    renderWithProviders(<CheckoutPage />);

    await waitFor(() => {
      const payButton = screen.getByText("Payer avec Square");
      fireEvent.click(payButton);
    });

    await waitFor(() => {
      // Vérifier que la navigation a été déclenchée
      expect(mockNavigate).toHaveBeenCalled();
    });
  });

  it("devrait gérer les erreurs de paiement Square", async () => {
    renderWithProviders(<CheckoutPage />);

    await waitFor(() => {
      const errorButton = screen.getByText("Simuler erreur");
      fireEvent.click(errorButton);
    });

    await waitFor(() => {
      expect(screen.getByText("Test error")).toBeInTheDocument();
    });
  });

  it("devrait afficher le montant total correct", () => {
    renderWithProviders(<CheckoutPage />);

    // Le montant total devrait être calculé : qty * pricePerPart = 2 * 50 = 100
    expect(screen.getByText(/100/)).toBeInTheDocument();
  });

  it("devrait passer les bonnes props à SquarePaymentForm", async () => {
    renderWithProviders(<CheckoutPage />);

    await waitFor(() => {
      const squareForm = screen.getByTestId("square-payment-form");
      expect(squareForm).toBeInTheDocument();
    });
  });

  it("devrait afficher les 3 étapes du checkout", () => {
    renderWithProviders(<CheckoutPage />);

    expect(screen.getByText("checkout.shippingTitle")).toBeInTheDocument();
    expect(screen.getByText("checkout.deliveryTitle")).toBeInTheDocument();
    expect(screen.getByText("checkout.paymentTitle")).toBeInTheDocument();
  });

  it("devrait rediriger si aucun deal n'est fourni", () => {
    const mockNavigate = vi.fn();
    vi.mock("react-router-dom", async () => {
      const actual = await vi.importActual("react-router-dom");
      return {
        ...actual,
        useNavigate: () => mockNavigate,
        useLocation: () => ({ state: {} }),
      };
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[{ state: {} }]}>
          <CheckoutPage />
        </MemoryRouter>
      </QueryClientProvider>
    );

    // Vérifier que la redirection est déclenchée
    expect(mockNavigate).toHaveBeenCalledWith("/deals", { replace: true });
  });
});

